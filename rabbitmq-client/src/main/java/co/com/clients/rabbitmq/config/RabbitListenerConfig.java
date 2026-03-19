package co.com.clients.rabbitmq.config;

import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.MethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitListenerConfig implements RabbitListenerConfigurer {

	@Value("${info.app.apiversion}")
	String apiVersion;

	@Override
	public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
		
		RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry = new RabbitListenerEndpointRegistry() {
			
			@Override
			public void registerListenerContainer(RabbitListenerEndpoint endpoint, RabbitListenerContainerFactory<?> factory,
					boolean startImmediately) {
				
				String version = "v" + apiVersion;
				
				if (endpoint instanceof MethodRabbitListenerEndpoint methodEndpoint) {
					String[] queues = methodEndpoint.getQueueNames().toArray(new String[0]);

					for (int i = 0; i < queues.length; i++) {
						String queue = queues[i];
						if (!queue.endsWith(version)) {
							queues[i] = queue + version;
						}
					}

					methodEndpoint.setQueueNames(queues);
				}
				super.registerListenerContainer(endpoint, factory, true);
			}
			
		};
		
		
		
		registrar.setEndpointRegistry(rabbitListenerEndpointRegistry);
		
	}
}
