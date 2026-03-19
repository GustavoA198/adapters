package co.com.clients.parent.utility;

import static co.com.clients.parent.utility.ConstantField.STATUS_ERROR;

import java.util.Date;
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

import co.com.clients.parent.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@RequiredArgsConstructor
public class AsyncGatewaySimple {
	
	@Value("${parent.gateway.async.timeout}")
	private Long timeout;

	/**
	 * Ejecuta la lógica async sin Canonical.
	 *
	 * @param request    Supplier<Object> que contiene la operación a ejecutar
	 * @param webRequest HttpServletRequest para obtener path
	 */
	@SuppressWarnings("unchecked")
	public <T> ResponseEntity<?> makeRequest(Supplier<Object> request, HttpServletRequest webRequest) {

		String path = webRequest.getHeader("X-Forwarded-Prefix");
		
		if (Utilities.isNullOrEmptyField(path)) {
			path = webRequest.getServletPath();
		}

		CompletableFuture<Object> future = CompletableFuture.supplyAsync(request);

		try {
			// Obtener resultado dentro del timeout
			Object operationData = future.get(timeout, TimeUnit.SECONDS);

			if (operationData instanceof CompletableFuture<?> cf) {
				operationData = cf.get();
			}

			// Si el request devolviÃ³ una excepciÃ³n de negocio
			if (operationData instanceof AppException ex) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex);
			}

			// Respuesta normal OK
			return ResponseEntity.status(HttpStatus.OK).body((T) operationData);

		} catch (TimeoutException e) {
			log.debug("Proceso demorado mÃ¡s de {} segundos para el path: {}", timeout, path);
			// Proceso demorado -> retorno inmediato con estado "pending"
			return ResponseEntity.status(HttpStatus.ACCEPTED) // 202 Accepted
					.body("PROCESSING: " + path + " - " + new Date());
		}

		catch (Exception e) {
			log.debug("Error interno en el procesamiento del path: {}", path, e);
			AppException ex = new AppException();
			ex.setCode(STATUS_ERROR);
			ex.setMessage(e.getMessage());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex);
		}
	}

	private void sendFinalResponse(Object body, String callbackUrl) {
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Object> entity = new HttpEntity<>(body, headers);

		log.info("Enviando respuesta final al callback URL: {}", callbackUrl);
		restTemplate.postForEntity(callbackUrl, entity, String.class);
	}

}
