package co.com.clients.rabbitmq.service;

import java.util.Map;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.amqp.dsl.AmqpInboundChannelAdapterSpec;
import org.springframework.integration.amqp.dsl.AmqpOutboundChannelAdapterSpec;
import org.springframework.integration.amqp.dsl.AmqpOutboundGatewaySpec;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@Scope("singleton")
@RequiredArgsConstructor
public class RabbitMQServiceImpl implements RabbitMQService {

	@Value("${info.app.apiversion}")
	String apiVersion;

	@Value("${spring.rabbitmq.queue.create.enable:false}")
	Boolean createQueue;

	private final RabbitTemplate rabbitTemplate;
	private final RabbitAdmin rabbitAdmin;

	@Override
	public AmqpOutboundGatewaySpec sendAndReceive(String queueName) {
		if (createQueue) {
			// Se crea la cola si no existe
			createQueueIfNotExists(transform(queueName), rabbitAdmin);
		}
		return Amqp.outboundGateway(rabbitTemplate).routingKey(transform(queueName));

	}

	@Override
	public AmqpOutboundChannelAdapterSpec send(String queueName) {
		if (createQueue) {
			// Se crea la cola si no existe
			createQueueIfNotExists(queueName, rabbitAdmin);
		}
		return Amqp.outboundAdapter(rabbitTemplate).routingKey(queueName);
	}

	@Override
	public AmqpOutboundChannelAdapterSpec sendTopic(String topic) {
		if (createQueue) {
			// Se crea el topico si no existe
			this.rabbitAdmin.declareExchange(new TopicExchange(topic));
		}
		return Amqp.outboundAdapter(rabbitTemplate).exchangeName(topic).routingKey(topic);
	}

	@Override
	public AmqpInboundChannelAdapterSpec receive(String queueName) {
		if (createQueue) {
			// Se crea la cola si no existe
			createQueueIfNotExists(transform(queueName), rabbitAdmin);
		}
		return Amqp.inboundAdapter(rabbitTemplate.getConnectionFactory(), transform(queueName))
				.messageConverter(rabbitTemplate.getMessageConverter());
	}

	@Override
	public Object sendAndReceive(String queueName, Object message) {
		if (createQueue) {
			// Se crea la cola si no existe
			createQueueIfNotExists(queueName, rabbitAdmin);
		}
		// Se envía el mensaje y se espera la respuesta
		return rabbitTemplate.convertSendAndReceive(queueName, message, msg -> {
			msg.getMessageProperties().setHeader("X-Target-Queue", queueName);
			return msg;
		});
	}

	@Override
	public void send(String queueName, Object message) {
		if (createQueue) {
			// Se crea la cola si no existe
			createQueueIfNotExists(queueName, rabbitAdmin);
		}
		// Se envía el mensaje y se espera la respuesta
		rabbitTemplate.convertAndSend(queueName, message, msg -> {
			msg.getMessageProperties().setHeader("X-Target-Queue", queueName);
			return msg;
		});
	}

	@Override
	public void sendTopic(String topic, Object message) {
		if (createQueue) {
			// Se crea la cola si no existe
			this.rabbitAdmin.declareExchange(new TopicExchange(topic));
		}
		// Se envía el mensaje y se espera la respuesta
		rabbitTemplate.convertAndSend(topic, topic, message, msg -> {
			msg.getMessageProperties().setHeader("X-Target-Queue", topic);
			return msg;
		});
	}

	private void createQueueIfNotExists(String queueName, RabbitAdmin rabbitAdmin) {
		Queue queue = new Queue(queueName, true); // true indica que la cola es duradera
		rabbitAdmin.declareQueue(queue);
	}

	private String transform(String routingKey) {
		if (routingKey == null) {
			return null;
		}
		String result = routingKey;

		String version = "v" + apiVersion;

		if (!result.endsWith(version)) {
			result = result + version;
		}
		return result;
	}

	@Override
	public void sendTopic(String topic, Object message, Map<String, Object> headers) {
		if (createQueue) {
			this.rabbitAdmin.declareExchange(new TopicExchange(topic));
		}

		rabbitTemplate.convertAndSend(topic, topic, message, msg -> {
			msg.getMessageProperties().setHeader("X-Target-Queue", topic);
			if (headers != null) {
				headers.forEach((k, v) -> msg.getMessageProperties().setHeader(k, v));
			}
			return msg;
		});
	}

}