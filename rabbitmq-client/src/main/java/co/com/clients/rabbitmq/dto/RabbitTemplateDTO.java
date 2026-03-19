package co.com.clients.rabbitmq.dto;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.rabbitmq.client.Channel;

public class RabbitTemplateDTO extends RabbitTemplate {

    public RabbitTemplateDTO(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public void doSend(Channel channel, String exchange, String routingKey, Message message, boolean mandatory, CorrelationData correlationData) {
        message.getMessageProperties().setHeader("X-Target-Queue", routingKey);
        System.out.println("Interceptando envío: routingKey = " + routingKey);

        super.doSend(channel, exchange, routingKey, message, mandatory, correlationData);
    }
}
