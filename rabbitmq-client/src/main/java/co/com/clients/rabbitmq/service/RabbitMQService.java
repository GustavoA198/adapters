package co.com.clients.rabbitmq.service;

import java.util.Map;

import org.springframework.integration.amqp.dsl.AmqpInboundChannelAdapterSpec;
import org.springframework.integration.amqp.dsl.AmqpOutboundChannelAdapterSpec;
import org.springframework.integration.amqp.dsl.AmqpOutboundGatewaySpec;

public interface RabbitMQService {

	public AmqpOutboundGatewaySpec sendAndReceive(String queueName);
	
	public AmqpOutboundChannelAdapterSpec send(String queueName);
	
	public AmqpOutboundChannelAdapterSpec sendTopic(String topic);
	
    public AmqpInboundChannelAdapterSpec receive(String queueName) ;
    
    public Object sendAndReceive(String queueName, Object request);
    
    public void send(String queueName, Object request);
    
    public void sendTopic(String topic, Object request);
    
    public void sendTopic(String topic, Object message, Map<String, Object> headers);
	
}
