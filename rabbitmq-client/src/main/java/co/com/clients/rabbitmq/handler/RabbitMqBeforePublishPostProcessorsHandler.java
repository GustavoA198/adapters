package co.com.clients.rabbitmq.handler;

import java.util.List;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RabbitMqBeforePublishPostProcessorsHandler implements MessagePostProcessor {

	private final List<String> dlxQueueList;

	private final List<String> exchangeMonitorList;

	@Override
	public Message postProcessMessage(Message message) throws AmqpException {
		String typeId = message.getMessageProperties().getHeader("__TypeId__");
		String body = new String(message.getBody());

		// Obtengo el nombre de la cola
		String queueName = message.getMessageProperties().getHeader("X-Target-Queue");
		if (exchangeMonitorList != null && !exchangeMonitorList.isEmpty() && exchangeMonitorList.contains(queueName)) {

			// Si el mensaje es de tipo lista, se agrega un header con el mensaje original
			message.getMessageProperties().getHeaders().put("originalMessage", body);
			message.getMessageProperties().getHeaders().put("monitor", true);
		}

		if (dlxQueueList != null && !dlxQueueList.isEmpty() && dlxQueueList.contains(queueName)) {
			message.getMessageProperties().getHeaders().put("dlx", true);
		}

		if (typeId != null && typeId.equals("java.lang.String") && message.getMessageProperties().getHeaders().get("sqs") != null) {

			body = body.replace("\\\"", "\"");
			// Se elimina el primer y el ultimo "
			body = body.substring(1, body.length() - 1);

			Message newMessage = new Message(body.getBytes(), message.getMessageProperties());
			return newMessage;
		}
		return message;
	}

}
