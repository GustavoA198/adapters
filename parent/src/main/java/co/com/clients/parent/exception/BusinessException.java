package co.com.clients.parent.exception;

/**
 * @author Zathura Code Generator Version 24.05 http://zathuracode.org/
 * @generationDate 2024-08-05T08:45:56.708048
 */
public class BusinessException extends BackendException {

	private static final long serialVersionUID = 1L;

	protected BusinessException(String exception) {
		super(ErrorType.BUSINESS, exception);
	}

	protected BusinessException(String exception, Exception e) {
		super(ErrorType.BUSINESS, exception, e);
	}
	
	protected BusinessException(String exception, String... params) {
		super(ErrorType.BUSINESS, exception, params);
	}

	protected BusinessException(ErrorMessage exception) {
		super(ErrorType.BUSINESS, exception.getValue());
	}

	protected BusinessException(ErrorMessage exception, Exception e) {
		super(ErrorType.BUSINESS, exception.getValue(), e);
	}

	protected BusinessException(ErrorMessage exception, String... params) {
		super(ErrorType.BUSINESS, exception.getValue(), params);
	}

	protected BusinessException(ErrorMessage exception, Exception e, String... params) {
		super(ErrorType.BUSINESS, exception.getValue(), e, params);
	}
	
	protected BusinessException(String code, String exception) {
		super(code, exception);
	}

	protected BusinessException(String code, String exception, Exception e) {
		super(code, exception, e);
	}
	
	public static class BackendException extends BusinessException {
		private static final long serialVersionUID = 1L;

		public BackendException(String code, String exception) {
			super(code, exception);
		}
	}
	
	public static class ValueNotFoundException extends BusinessException {
		private static final long serialVersionUID = 1L;

		public ValueNotFoundException(String value) {
			super(ErrorMessage.VALUE_NOT_FOUND, value);
		}
	}
	
	public static class ValueNotInCatalogException extends BusinessException {
		private static final long serialVersionUID = 1L;

		public ValueNotInCatalogException(String catalogTable, String value) {
			super(ErrorMessage.VALUE_NOT_IN_CATALOG, value, catalogTable);
		}
	}
} 


