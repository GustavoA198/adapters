package co.com.clients.parent.exception;

/**
 * @author Zathura Code Generator Version 24.05 http://zathuracode.org/
 * @generationDate 2024-08-05T08:45:56.708048
 */
public class ValidationException extends BackendException {

	private static final long serialVersionUID = 1L;

	protected ValidationException(String exception) {
		super(ErrorType.VALIDATION, exception);
	}

	protected ValidationException(String exception, Exception e) {
		super(ErrorType.VALIDATION, exception, e);
	}

	protected ValidationException(String exception, String... params) {
		super(ErrorType.VALIDATION, exception, params);
	}

	protected ValidationException(ErrorMessage exception) {
		super(ErrorType.VALIDATION, exception.getValue());
	}

	protected ValidationException(ErrorMessage exception, Exception e) {
		super(ErrorType.VALIDATION, exception.getValue(), e);
	}

	protected ValidationException(ErrorMessage exception, String... params) {
		super(ErrorType.VALIDATION, exception.getValue(), params);
	}

	protected ValidationException(ErrorMessage exception, Exception e, String... params) {
		super(ErrorType.VALIDATION, exception.getValue(), e, params);
	}

	public static class RequestEmptyException extends ValidationException {
		private static final long serialVersionUID = 1L;

		public RequestEmptyException() {
			super(ErrorMessage.REQUEST_EMPTY);
		}
	}

	public static class FieldRequiredException extends ValidationException {
		private static final long serialVersionUID = 1L;

		public FieldRequiredException(String field) {
			super(ErrorMessage.FIELD_REQUIRED, field);
		}
	}

	public static class FieldNotValidException extends ValidationException {
		private static final long serialVersionUID = 1L;

		public FieldNotValidException(String field) {
			super(ErrorMessage.FIELD_NOT_VALID, field);
		}
	}

	public static class FieldMinLengthException extends ValidationException {
		private static final long serialVersionUID = 1L;

		public FieldMinLengthException(String field, int minLength) {
			super(ErrorMessage.FIELD_MIN_LENGTH, field, String.valueOf(minLength));
		}
	}

	public static class FieldMaxLengthException extends ValidationException {
		private static final long serialVersionUID = 1L;

		public FieldMaxLengthException(String field, int maxLength) {
			super(ErrorMessage.FIELD_MAX_LENGTH, field, String.valueOf(maxLength));
		}
	}

	public static class FieldEmptyException extends ValidationException {
		private static final long serialVersionUID = 1L;

		public FieldEmptyException(String field) {
			super(ErrorMessage.FIELD_EMPTY, field);
		}
	}
	
	public static class FieldMinValueException extends ValidationException {
		private static final long serialVersionUID = 1L;

		public FieldMinValueException(String field, int minLength) {
			super(ErrorMessage.FIELD_MIN_VALUE, field, String.valueOf(minLength));
		}
	}

	public static class FieldMaxValueException extends ValidationException {
		private static final long serialVersionUID = 1L;

		public FieldMaxValueException(String field, int maxLength) {
			super(ErrorMessage.FIELD_MAX_VALUE, field, String.valueOf(maxLength));
		}
	}
}
