package co.com.clients.parent.config.scope;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class OperationScope implements Scope {

    private final ConcurrentHashMap<String, Map<String, Object>> storage =
            new ConcurrentHashMap<>();

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {

        String operationId = OperationContextHolder.getOperationId();

        if (operationId == null) {
            throw new IllegalStateException(
                "No active operationId found. Did you forget to start the operation?"
            );
        }

        storage.putIfAbsent(operationId, new ConcurrentHashMap<>());

        return storage.get(operationId)
                .computeIfAbsent(name, n -> objectFactory.getObject());
    }

    @Override
    public Object remove(String name) {

        String operationId = OperationContextHolder.getOperationId();
        if (operationId == null) {
            return null;
        }

        Map<String, Object> scopedObjects = storage.get(operationId);
        if (scopedObjects != null) {
            return scopedObjects.remove(name);
        }

        return null;
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        // Opcional.
        // Spring no gestiona destrucciÃ³n automÃ¡tica en scopes custom.
        // Puedes guardar el callback y ejecutarlo en clear() si lo necesitas.
    }

    @Override
    public Object resolveContextualObject(String key) {
        // No usamos objetos contextuales adicionales
        return null;
    }

    @Override
    public String getConversationId() {
        return OperationContextHolder.getOperationId();
    }

    /**
     * Limpia completamente el scope de la operaciÃ³n actual.
     * Debe llamarse al finalizar HTTP / mensaje / cron.
     */
    public void clear() {

        String operationId = OperationContextHolder.getOperationId();
        if (operationId != null) {
            log.debug("Clearing operation scope for operationId={}", operationId);
            storage.remove(operationId);
        }
    }
}