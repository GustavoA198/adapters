package co.com.clients.rabbitmq.handler;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import co.com.clients.rabbitmq.dto.MonitorDTO;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RabbitMqAfterReceivePostProcessorsHandler implements MessagePostProcessor {

	private final RabbitTemplate rabbitTemplate;
	private final String exchangeMonitor;

	@Override
	public Message postProcessMessage(Message message) throws AmqpException {
		// Se ejecuta cuando se lee el mensaje de la cola
		if (message.getMessageProperties().getHeaders().containsKey("originalMessage") && message.getMessageProperties().getHeaders().containsKey("monitor")) {
			// Si el mensaje es de tipo lista, se agrega un header con el mensaje original
			String request = message.getMessageProperties().getHeaders().get("originalMessage").toString();
			String response = new String(message.getBody());

			String queueName = message.getMessageProperties().getHeader("X-Target-Queue");

			MonitorDTO monitorDTO = MonitorDTO.builder().request(request).response(response).queue(queueName).build();

			rabbitTemplate.convertAndSend(exchangeMonitor, queueName, monitorDTO);

		}

		return message;
	}

}
