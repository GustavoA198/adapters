package co.com.clients.parent.utility;

import static co.com.clients.parent.utility.ConstantField.STATUS_ERROR;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import co.com.clients.parent.config.scope.OperationContextHolder;
import co.com.clients.parent.config.scope.OperationContextUtility;
import co.com.clients.parent.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@Scope("singleton")
@RequiredArgsConstructor
public class SyncGateway {

	private final OperationContextUtility operationContextUtility;

	@SuppressWarnings("unchecked")
	public <T> ResponseEntity<T> makeRequest(Supplier<Object> request,
			HttpServletRequest webRequest) {

		String path = webRequest.getHeader("X-Forwarded-Prefix");
		if (path == null || path.isBlank()) {
			path = webRequest.getServletPath();
		}

		operationContextUtility.start();

		try {
			Object operationData = request.get();
			if (operationData instanceof CompletableFuture<?> cf) {
				operationData = cf.get();
			}
			if (operationData instanceof AppException excepcion) {
				OperationContextHolder.end();
				return (ResponseEntity<T>) ResponseEntity.badRequest().body(excepcion);
			}

			OperationContextHolder.end();
			return ResponseEntity.ok((T) operationData);

		} catch (Exception e) {
			AppException cineappException = new AppException();
			cineappException.setCode(STATUS_ERROR);
			cineappException.setMessage(e.getMessage());
			log.error("Error en SyncGateway", e);
			OperationContextHolder.end();
			return (ResponseEntity<T>) ResponseEntity.internalServerError().body(cineappException);
		}
	}

	/**
	 * Compatibilidad temporal: mantiene llamadas existentes con 3 argumentos,
	 * ignorando el payload legado.
	 */
	public <T> ResponseEntity<T> makeRequest(Supplier<Object> request,
			Object legacyRequest, HttpServletRequest webRequest) {
		return makeRequest(request, webRequest);
	}
}
