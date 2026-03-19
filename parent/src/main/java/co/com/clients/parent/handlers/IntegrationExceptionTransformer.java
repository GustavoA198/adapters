package co.com.clients.parent.handlers;

import org.springframework.integration.transformer.AbstractTransformer;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import co.com.clients.parent.exception.AppException;
import co.com.clients.parent.exception.BackendException;
import co.com.clients.parent.service.MessageService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IntegrationExceptionTransformer extends AbstractTransformer {

	private final MessageService messageService;

	@Override
	public Object doTransform(Message<?> message) {

		AppException cineappException = new AppException();

		BackendException vbException = (BackendException) message.getPayload();
		String messageError = messageService.getMessage(vbException.getException(), vbException.getParams());
		messageError = !messageError.isEmpty() ? messageError : vbException.getMessage();
		cineappException
				.setCode(vbException.getCustomError() != null ? vbException.getCustomError() : vbException.getException());
		cineappException.setMessage(messageError);
		cineappException.setErrorType(vbException.getErrorType());
		return cineappException;
	}
}
