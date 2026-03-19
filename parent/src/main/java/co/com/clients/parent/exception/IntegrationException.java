package co.com.clients.parent.exception;

/**
 * Backward-compatible exception alias used by legacy adapters.
 */
public class IntegrationException extends BackendException {

    private static final long serialVersionUID = 1L;

    public IntegrationException(ErrorType errorType, String exception, String... params) {
        super(errorType, exception, params);
    }

    public IntegrationException(ErrorType errorType, String exception, Exception e, String... params) {
        super(errorType, exception, e, params);
    }

    public IntegrationException(String errorType, String exception, Exception e, String... params) {
        super(errorType, exception, e, params);
    }

    public IntegrationException(String errorType, String exception, String... params) {
        super(errorType, exception, params);
    }
}

