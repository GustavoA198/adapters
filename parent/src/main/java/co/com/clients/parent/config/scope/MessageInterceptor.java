package co.com.clients.parent.config.scope;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

/**
 * Interceptor de canales Spring Integration.
 * Simplificado: ya no propaga headers del canonical request.
 */
@Component
@Log4j2
public class MessageInterceptor implements ChannelInterceptor {

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		log.debug("Message intercepted on channel: {}", channel);
		return message;
	}
}
