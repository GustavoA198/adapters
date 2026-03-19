package co.com.clients.parent.config;

import static co.com.clients.parent.utility.ConstantField.ERROR_MESSAGE_TEMPLATE;
import static co.com.clients.parent.utility.ConstantField.FIELD_ERROR_SEPARATOR;
import static co.com.clients.parent.utility.ConstantField.STATUS_ERROR;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import co.com.clients.parent.exception.AppException;
import co.com.clients.parent.exception.BackendException;
import co.com.clients.parent.service.MessageService;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;

/**
 * Handle all exceptions and java bean validation errors
 * for all endpoints income data that use the @Valid annotation.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {

	private final MessageService messageService;

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<String> validationErrors = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + FIELD_ERROR_SEPARATOR + error.getDefaultMessage()).toList();
		return getExceptionResponseEntity(exception, HttpStatus.BAD_REQUEST, validationErrors);
	}

	@Override
	@Nullable
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException exception,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		Throwable cause = exception.getCause();
		String resolvedMessage = exception.getLocalizedMessage();

		if (cause instanceof InvalidFormatException invalidFormatException) {
			String path = invalidFormatException.getPath().stream().map(ref -> ref.getFieldName())
					.collect(Collectors.joining("."));
			resolvedMessage = messageService.getMessage("notReadableException", path,
					invalidFormatException.getTargetType().getSimpleName());
		} else if (cause instanceof JsonMappingException jsonMappingException) {
			String path = jsonMappingException.getPath().stream().map(ref -> ref.getFieldName())
					.collect(Collectors.joining("."));
			String tipo = getTargetType(jsonMappingException);
			resolvedMessage = messageService.getMessage("notReadableException", path, tipo);
		}

		return getExceptionResponseEntity(exception, HttpStatus.BAD_REQUEST,
				Collections.singletonList(resolvedMessage));
	}

	private String getTargetType(JsonMappingException ex) {
		if (ex.getPath() == null || ex.getPath().isEmpty()) {
			return "Desconocido";
		}

		JsonMappingException.Reference lastRef = ex.getPath().get(ex.getPath().size() - 1);
		Object fromObject = lastRef.getFrom();
		String fieldName = lastRef.getFieldName();

		if (fromObject == null || fieldName == null) {
			return "Desconocido";
		}

		Field field = findFieldByJsonProperty(fromObject.getClass(), fieldName);
		return field != null ? field.getType().getSimpleName() : "Desconocido";
	}

	private Field findFieldByJsonProperty(Class<?> clazz, String jsonFieldName) {
		while (clazz != null) {
			for (Field field : clazz.getDeclaredFields()) {
				JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
				if (jsonProperty != null && jsonProperty.value().equals(jsonFieldName)) {
					return field;
				}
				if (field.getName().equalsIgnoreCase(jsonFieldName)) {
					return field;
				}
			}
			clazz = clazz.getSuperclass();
		}
		return null;
	}

	private ResponseEntity<Object> getExceptionResponseEntity(final Exception exception, final HttpStatus status,
			final List<String> errors) {

		String exceptionMessage = (errors != null && !errors.isEmpty())
				? String.join(",", errors)
				: (exception != null ? exception.getMessage() : status.getReasonPhrase());

		AppException appException = new AppException();
		appException.setCode(STATUS_ERROR);
		appException.setMessage(exceptionMessage);

		return new ResponseEntity<>(appException, status);
	}

	@ExceptionHandler({ ConstraintViolationException.class })
	public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException exception, WebRequest request) {
		final List<String> validationErrors = exception.getConstraintViolations().stream()
				.map(violation -> violation.getPropertyPath() + FIELD_ERROR_SEPARATOR + violation.getMessage()).toList();
		return getExceptionResponseEntity(exception, HttpStatus.BAD_REQUEST, validationErrors);
	}

	@ExceptionHandler({ Exception.class })
	public ResponseEntity<Object> handleAllExceptions(Exception exception, WebRequest request) {
		ResponseStatus responseStatus = exception.getClass().getAnnotation(ResponseStatus.class);
		final HttpStatus status = responseStatus != null ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;
		final String localizedMessage = exception.getLocalizedMessage();
		final String path = request.getDescription(false);
		String message = !localizedMessage.isEmpty() ? localizedMessage : status.getReasonPhrase();
		logger.error(String.format(ERROR_MESSAGE_TEMPLATE, message, path), exception);
		return getExceptionResponseEntity(exception, status, Collections.singletonList(message));
	}

	@ExceptionHandler({ BackendException.class })
	public ResponseEntity<Object> handleAllExceptions(BackendException exception, WebRequest request) {
		if (exception == null) {
			return getExceptionResponseEntity(null, HttpStatus.INTERNAL_SERVER_ERROR,
					Collections.singletonList("null"));
		}
		final HttpStatus status = exception.getErrorType() != null
				? HttpStatus.valueOf(exception.getErrorType().getValue())
				: HttpStatus.INTERNAL_SERVER_ERROR;

		String message = messageService.getMessage(exception.getException(), exception.getParams());
		message = !message.isEmpty() ? message : status.getReasonPhrase();

		final String path = request.getDescription(false);
		if (!exception.getErrorType().getValue().equals("400")) {
			logger.error(String.format(ERROR_MESSAGE_TEMPLATE, message, path), exception);
		}
		return getExceptionResponseEntity(exception, status, Collections.singletonList(message));
	}
}
