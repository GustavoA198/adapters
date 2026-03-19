package co.com.clients.parent.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Zathura Code Generator Version 24.05 http://zathuracode.org/
 * @generationDate 2024-08-05T08:45:56.708048
 */
@Getter
@Setter
public class BackendException extends Exception {

	private static final long serialVersionUID = 1L;
	private final ErrorType errorType;
	private final String exception;
	private final String customError;
	private final String[] params;

	public BackendException(ErrorType errorType, String exception, String... params) {
		this.errorType = errorType;
		this.exception = exception;
		this.params = params;
		this.customError = null;
	}
	
	public BackendException(ErrorType errorType, String exception, Exception e, String... params) {
		super(e);
		this.errorType = errorType;
		this.exception = exception;
		this.params = params;
		this.customError = null;
	}

	public BackendException(String errorType, String exception, Exception e, String... params) {
		super(e);
		this.errorType = ErrorType.BUSINESS;
		this.exception = exception;
		this.params = params;
		this.customError = errorType;
	}

	public BackendException(String errorType, String exception, String... params) {
		this.errorType = ErrorType.BUSINESS;
		this.exception = exception;
		this.params = params;
		this.customError = errorType;
	}
}
