package co.com.clients.parent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.handler.advice.ErrorMessageSendingRecoverer;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.store.MessageStore;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import co.com.clients.parent.config.scope.MessageInterceptor;
import co.com.clients.parent.handlers.IntegrationExceptionTransformer;
import co.com.clients.parent.utility.ConstantField;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class IntegrationFlowConfig {

	@Value("${parent.gateway.retry.maxAttempts}")
	private Integer maxAttempts;
	@Value("${parent.gateway.retry.delay}")
	private Integer delay;
	
	private final IntegrationExceptionTransformer cineappExceptionTransformer;
	private final MessageInterceptor messageInterceptor;

	// ConfiguraciÃ³n de polÃ­tica de reintento
	@Bean
	RequestHandlerRetryAdvice retryAdvice() {
		RequestHandlerRetryAdvice retryAdvice = new RequestHandlerRetryAdvice();
		RetryTemplate retryTemplate = new RetryTemplate();

		// ConfiguraciÃ³n de la polÃ­tica de reintento
		RetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts);
		retryTemplate.setRetryPolicy(retryPolicy);

		// ConfiguraciÃ³n de la polÃ­tica de retroceso
		FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
		backOffPolicy.setBackOffPeriod(delay);
		retryTemplate.setBackOffPolicy(backOffPolicy);

		retryAdvice.setRetryTemplate(retryTemplate);

		// ConfiguraciÃ³n de recuperaciÃ³n en caso de fallo
		
		retryAdvice.setRecoveryCallback(new ErrorMessageSendingRecoverer(errorChannel()));

		return retryAdvice;
	}

	// Canal de errores para manejar mensajes fallidos
	private MessageChannel errorChannel() {
		return MessageChannels.direct().getObject();
	}
	
	@Bean
	MessageStore messageStore() {
	    return new SimpleMessageStore();
	}
	
	@Bean
    IntegrationFlow errorExceptionFlow() {
        return IntegrationFlow.from(ConstantField.ERROR_CHANNEL).transform(cineappExceptionTransformer).get();
    }
	
	
	@Bean(name = "gatewayExecutor")
    ThreadPoolTaskExecutor gatewayExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(0);
        executor.setMaxPoolSize(Integer.MAX_VALUE);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("gateway-vt-");
        executor.setVirtualThreads(true);
        
        executor.initialize();
        return executor;
    }
	
	@Bean
    @GlobalChannelInterceptor(patterns = "*")
    ChannelInterceptor canonicalInterceptor() {
        return messageInterceptor;
    }
}
