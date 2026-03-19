package co.com.clients.rabbitmq.config;

import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.support.AmqpHeaderMapper;
import org.springframework.integration.amqp.support.DefaultAmqpHeaderMapper;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.com.clients.rabbitmq.dto.RabbitTemplateDTO;
import co.com.clients.rabbitmq.handler.RabbitMqAfterReceivePostProcessorsHandler;
import co.com.clients.rabbitmq.handler.RabbitMqBeforePublishPostProcessorsHandler;
import co.com.clients.rabbitmq.handler.RabbitMqBeforeReadMessageHandler;
import co.com.clients.parent.config.scope.OperationContextUtility;
import co.com.clients.parent.exception.AppException;
import co.com.clients.parent.exception.ErrorType;
import co.com.clients.parent.exception.IntegrationException;
import co.com.clients.parent.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@EnableRabbit
@Configuration
@Log4j2
@RequiredArgsConstructor
public class RabbitConfig {

	private final MessageService messageService;
	
	private final OperationContextUtility operationContextUtility;
	
	@Value("#{'${spring.rabbitmq.exchange.monitor.list:}'.split(',')}")
	private List<String> exchangeMonitorList;

	@Value("${spring.rabbitmq.exchange.monitor.name:monitorExchange}")
	private String exchangeMonitor;
	
	@Value("#{'${spring.rabbitmq.exchange.dlx.list:}'.split(',')}")
	private List<String> dlxQueueList;
	
	@Value("${spring.rabbitmq.exchange.dlx.name:}")
	private String dlxExchange;

	@Value("${spring.rabbitmq.exchange.dlx.seconds-to-retry:30}")
	private int dlxSecondsToRetry;

	@Value("${spring.rabbitmq.exchange.dlx.max-attempts:3}")
	private int dlxMaxAttempts;
	
	@Value("${spring.rabbitmq.listener.simple.concurrency:5}")
	private int listenerConcurrency;
	
	@Value("${spring.rabbitmq.listener.simple.max-concurrency:15}")
	private int listenerMaxConcurrency;
	
	@Value("${spring.rabbitmq.listener.simple.prefetch:5}")
	private int listenerPrefetch;
	
	@Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplateDTO template = new RabbitTemplateDTO(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());  // Usar Jackson para convertir mensajes
        template.setBeforePublishPostProcessors(message -> new RabbitMqBeforePublishPostProcessorsHandler(dlxQueueList, exchangeMonitorList).postProcessMessage(message));

        RabbitMqAfterReceivePostProcessorsHandler rabbitMqAfterReceivePostProcessorsHandler = new RabbitMqAfterReceivePostProcessorsHandler(template, exchangeMonitor);
        template.setAfterReceivePostProcessors((message) -> rabbitMqAfterReceivePostProcessorsHandler.postProcessMessage(message));

        template.setReceiveTimeout(1200000);
        template.setReplyTimeout(1200000);

        return template;
    }
	
	@Bean
	public AmqpHeaderMapper customHeaderMapper() {
	    DefaultAmqpHeaderMapper mapper = DefaultAmqpHeaderMapper.outboundMapper();
	    mapper.setRequestHeaderNames("*");  // acepta todos los headers de request
	    mapper.setReplyHeaderNames("*");    // acepta todos los headers en reply
	    return mapper;
	}

    @Bean
    MessageConverter jsonMessageConverter() {
    	ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // Ignorar nulls
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")); // Formato de fecha ISO 8601
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
        return converter;
    }

    @Bean
    RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
	RabbitListenerErrorHandler rabbitMqErrorHandler() {
		return (amqpMessage, channel, message, exception) -> {
			// Log error

			// si el mensaje tiene el header de dlx, no se procesa el error
			if(amqpMessage.getMessageProperties().getHeaders().get("dlx") != null) {
				log.debug("El mensaje tiene el header dlx, no se procesa el error");
				throw exception;
			}
			
			// Si el mensaje no tiene replay to, se retorna null
			if(amqpMessage.getMessageProperties().getReplyTo() == null) {
                return null;
			}

			// Personaliza el manejo del error

			AppException cineappException = new AppException();
			IntegrationException vbException = null;
			
			try {
				if (exception instanceof ListenerExecutionFailedException) {

					ListenerExecutionFailedException listenerException = (ListenerExecutionFailedException) exception;
					Object cause = listenerException.getCause(); // Obtener la causa raÃ­z
					
					if (cause instanceof IntegrationException){
						vbException = (IntegrationException) cause;
					}
				} else if (exception.getCause() instanceof IntegrationException) {
					vbException = (IntegrationException) exception.getCause();
				}
				
				if (vbException != null) {
					String messageError = messageService.getMessage(vbException.getException(), vbException.getParams());
					messageError = !messageError.isEmpty() ? messageError : exception.getMessage();
					cineappException.setCode(vbException.getCustomError() != null ? vbException.getCustomError() : vbException.getException());
					cineappException.setMessage(messageError);
					cineappException.setErrorType(vbException.getErrorType());
				} else {
					log.error("Error al obtener el mensaje de error: {}", exception.getMessage(), exception);
					cineappException.setCode("500");
					if(exception.getCause() != null && exception.getCause().getMessage() != null && !exception.getCause().getMessage().isBlank()) {
						cineappException.setMessage(exception.getCause().getMessage());
					}else {
						cineappException.setMessage(exception.getMessage());
					}
					cineappException.setErrorType(ErrorType.SYSTEM);
				}

			} catch (Exception e) {
				log.error("Error al obtener el mensaje de error: {}", e.getMessage(), e);
				cineappException.setCode("500");
				if(exception.getCause() != null && exception.getCause().getMessage() != null && !exception.getCause().getMessage().isBlank()) {
					cineappException.setMessage(exception.getCause().getMessage());
				}else {
					cineappException.setMessage(exception.getMessage());
				}
				cineappException.setErrorType(ErrorType.SYSTEM);
			}

	        org.springframework.messaging.Message<AppException> errorMessage = MessageBuilder.withPayload(cineappException)
	        		.copyHeaders(amqpMessage.getMessageProperties().getHeaders()).build();
	        return errorMessage;
		};
	}
    
    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter messageConverter, RabbitTemplate rabbitTemplate) {
    	SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(listenerConcurrency);
        factory.setMaxConcurrentConsumers(listenerMaxConcurrency);
        factory.setPrefetchCount(listenerPrefetch);

        RabbitMqBeforeReadMessageHandler rabbitMqAfterReceivePostProcessorsHandler = new RabbitMqBeforeReadMessageHandler(operationContextUtility);
        factory.setAfterReceivePostProcessors((message) -> rabbitMqAfterReceivePostProcessorsHandler.postProcessMessage(message));
        
        // No reencolar
  		factory.setDefaultRequeueRejected(false);

  		// Inyectamos el advice con retry
  		if (dlxExchange != null && !dlxExchange.isEmpty()){
      		factory.setAdviceChain(monitorRetryInterceptor(rabbitTemplate));
  		}
        
        return factory;
    }
    
    @Bean
	RetryOperationsInterceptor monitorRetryInterceptor(RabbitTemplate tpl) {
		
		return RetryInterceptorBuilder.stateless()
			.maxAttempts(dlxMaxAttempts)
			.backOffOptions(dlxSecondsToRetry * 1000, 1.0, dlxSecondsToRetry * 1000)
			.recoverer((message, cause) -> {
				
				// Se valida si el message tiene el header dlx para enviar el mensaje a la dlx
				if(message.getMessageProperties().getHeaders().get("dlx") == null) {
					log.debug("El mensaje no tiene el header dlx, no se envÃ­a a la DLX");
					return;
				}
				
				String queueName = message.getMessageProperties().getHeader("X-Target-Queue");
				String dlqRoutingKey = queueName + ".dlq";
				
				// Agrega la excepciÃ³n al header "x-exception-message"
				String exceptionMessage = "??";

				log.debug("El error es de tipo: {}", cause.getClass().getSimpleName());

				if (cause instanceof ListenerExecutionFailedException) {

					ListenerExecutionFailedException listenerException = (ListenerExecutionFailedException) cause;
					cause = listenerException.getCause(); // Obtener la causa raÃ­z

					if (cause instanceof IntegrationException){
						
						IntegrationException vbException = (IntegrationException) cause;
						
						exceptionMessage = vbException.getException();
						String[] params = vbException.getParams();

						if (params != null && params.length > 0) {
							for (int i = 0; i < params.length; i++) {
								exceptionMessage += params[i] != null ? " - " + params[i] : ""; 
							}
						}

						log.info("El mensaje de excepciÃ³n es: {}", exceptionMessage);
					} else {
						exceptionMessage = cause != null ? cause.getMessage() : "Unknown error";
					}
				}
				
				message.getMessageProperties().getHeaders().put("x-exception-message-thrown", exceptionMessage);
				new RepublishMessageRecoverer(tpl, dlxExchange, dlqRoutingKey).recover(message, cause);
			})
			.build();
	}

}
