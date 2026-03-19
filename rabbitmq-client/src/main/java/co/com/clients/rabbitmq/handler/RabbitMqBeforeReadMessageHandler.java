package co.com.clients.rabbitmq.handler;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

import co.com.clients.parent.config.scope.OperationContextUtility;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RabbitMqBeforeReadMessageHandler implements MessagePostProcessor {

	private final OperationContextUtility operationContextUtility;

	@Override
	public Message postProcessMessage(Message message) throws AmqpException {
		operationContextUtility.start();
		return message;
	}

}
