package co.com.clients.parent.handlers;

import org.springframework.integration.core.GenericHandler;
import org.springframework.messaging.MessageHeaders;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class LogHandler implements GenericHandler<Object> {

	@Override
	public Object handle(Object response, MessageHeaders headers) {
		log.info("Se recibió un objeto de {} con los siguientes datos: {}", response.getClass().getName(), response);

		headers.forEach((key, value) -> log.info("Header: " + key + " = " + value));

		
		return response;
	}
}
