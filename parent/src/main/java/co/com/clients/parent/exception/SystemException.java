package co.com.clients.parent.exception;

/**
 * @author Zathura Code Generator Version 24.05 http://zathuracode.org/
 * @generationDate 2024-08-05T08:45:56.708048
 */
public class SystemException extends BackendException {

	private static final long serialVersionUID = 1L;

	protected SystemException(String exception) {
		super(ErrorType.SYSTEM, exception);
	}

	protected SystemException(String exception, Exception e) {
		super(ErrorType.SYSTEM, exception, e);
	}

	protected SystemException(String exception, String... params) {
		super(ErrorType.SYSTEM, exception, params);
	}

	protected SystemException(ErrorMessage exception) {
		super(ErrorType.SYSTEM, exception.getValue());
	}

	protected SystemException(ErrorMessage exception, Exception e) {
		super(ErrorType.SYSTEM, exception.getValue(), e);
	}

	protected SystemException(ErrorMessage exception, String... params) {
		super(ErrorType.SYSTEM, exception.getValue(), params);
	}

	protected SystemException(ErrorMessage exception, Exception e, String... params) {
		super(ErrorType.SYSTEM, exception.getValue(), e, params);
	}
	
	public static class ResponseServiceNullException extends SystemException {
		private static final long serialVersionUID = 1L;

		public ResponseServiceNullException(String serviceName) {
			super(ErrorMessage.RESPONSE_SERVICE_NULL, serviceName);
		}
	}
	
	public static class ResponseServiceErrorException extends SystemException {
		private static final long serialVersionUID = 1L;

		public ResponseServiceErrorException(String serviceName) {
			super(ErrorMessage.RESPONSE_SERVICE_ERROR, serviceName);
		}
	}
	
	public static class ResponseServiceErrorMappingException extends SystemException {
		private static final long serialVersionUID = 1L;

		public ResponseServiceErrorMappingException(String serviceName) {
			super(ErrorMessage.RESPONSE_SERVICE_ERROR_MAPPING, serviceName);
		}
	}
}
