package co.com.clients.parent.utility;

import static co.com.clients.parent.utility.ConstantField.MESSAGE_PENDING;
import static co.com.clients.parent.utility.ConstantField.STATUS_ERROR;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import co.com.clients.parent.config.scope.OperationContextHolder;
import co.com.clients.parent.config.scope.OperationContextUtility;
import co.com.clients.parent.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@RequiredArgsConstructor
public final class AsyncGateway {

	@Value("${parent.gateway.async.timeout}")
	private Long timeout;

	private final OperationContextUtility operationContextUtility;

	/**
	 * Ejecuta la lógica de forma asíncrona con soporte de timeout y callback.
	 *
	 * @param request     Supplier con la operación a ejecutar
	 * @param webRequest  HttpServletRequest para obtener el path
	 * @param callbackUrl URL opcional; si no aplica, pasar null o cadena vacía
	 */
	@SuppressWarnings("unchecked")
	public <T> ResponseEntity<T> makeRequest(Supplier<Object> request,
			HttpServletRequest webRequest, String callbackUrl) {

		String path = webRequest.getHeader("X-Forwarded-Prefix");
		if (path == null || path.isBlank()) {
			path = webRequest.getServletPath();
		}

		operationContextUtility.start();

		CompletableFuture<Object> future = CompletableFuture.supplyAsync(request);

		try {
			Object operationData = future.get(timeout, TimeUnit.SECONDS);
			if (operationData instanceof CompletableFuture<?> cf) {
				operationData = cf.get();
			}
			if (operationData instanceof AppException excepcion) {
				OperationContextHolder.end();
				return (ResponseEntity<T>) ResponseEntity.badRequest().body(excepcion);
			}

			OperationContextHolder.end();
			return ResponseEntity.ok((T) operationData);

		} catch (TimeoutException e) {
			log.debug("Proceso demorado más de {} segundos para el path: {}", timeout, path);
			// Si hay callbackUrl, enviar la respuesta final cuando termine
			if (callbackUrl != null && !callbackUrl.isBlank()) {
				future.thenAcceptAsync(result -> sendFinalResponse(result, callbackUrl));
			}
			return (ResponseEntity<T>) ResponseEntity.status(HttpStatus.ACCEPTED).body(MESSAGE_PENDING);

		} catch (Exception e) {
			AppException cineappException = new AppException();
			cineappException.setCode(STATUS_ERROR);
			cineappException.setMessage(e.getMessage());
			log.error("Error en AsyncGateway", e);
			OperationContextHolder.end();
			return (ResponseEntity<T>) ResponseEntity.internalServerError().body(cineappException);
		}
	}

	/**
	 * Sobrecarga sin callbackUrl para uso simple.
	 */
	public <T> ResponseEntity<T> makeRequest(Supplier<Object> request,
			HttpServletRequest webRequest) {
		return makeRequest(request, webRequest, (String) null);
	}

	/**
	 * Sobrecarga de compatibilidad: firma antigua con payload en segundo parámetro.
	 */
	public <T> ResponseEntity<T> makeRequest(Supplier<Object> request,
			Object legacyRequest, HttpServletRequest webRequest) {
		return makeRequest(request, webRequest, resolveCallbackUrl(legacyRequest));
	}

	private String resolveCallbackUrl(Object legacyRequest) {
		if (legacyRequest == null) {
			return null;
		}
		try {
			Method getHeader = legacyRequest.getClass().getMethod("getHeader");
			Object header = getHeader.invoke(legacyRequest);
			if (header == null) {
				return null;
			}
			Method getCallbackUrl = header.getClass().getMethod("getCallbackUrl");
			Object callback = getCallbackUrl.invoke(header);
			return callback instanceof String ? (String) callback : null;
		} catch (Exception e) {
			log.debug("No fue posible extraer callbackUrl del payload legado", e);
			return null;
		}
	}

	private void sendFinalResponse(Object order, String callbackUrl) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> requestEntity = new HttpEntity<>(order, headers);
		log.info("Enviando respuesta final al callback URL: {}", callbackUrl);
		restTemplate.postForEntity(callbackUrl, requestEntity, String.class);
		OperationContextHolder.end();
	}
}
