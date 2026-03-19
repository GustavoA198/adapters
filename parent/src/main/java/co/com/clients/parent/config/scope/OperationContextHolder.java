package co.com.clients.parent.config.scope;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class OperationContextHolder {

    private static final ThreadLocal<String> OPERATION_ID = new ThreadLocal<>();

    public static void start(String operationId) {
        log.info("Starting operation {}", operationId);
        OPERATION_ID.set(operationId);
    }

    public static String getOperationId() {
        return OPERATION_ID.get();
    }

    public static void end() {
        log.info("Ending operation {}", OPERATION_ID.get());
        OPERATION_ID.remove();
    }
}