package co.com.clients.parent.exception;

import lombok.Getter;

/**
 * @author Zathura Code Generator Version 24.05 http://zathuracode.org/
 * @generationDate 2024-08-05T08:45:56.708048
*/
@Getter
public enum ErrorType {

	VALIDATION("400"), BUSINESS("409"), SYSTEM("500");

	private final String value;

	ErrorType(String value) {
		this.value = value;
	}
	
	public static ErrorType fromValue(String value) {
		for (ErrorType errorType : ErrorType.values()) {
			if (errorType.getValue() == value) {
				return errorType;
			}
		}
		throw new IllegalArgumentException("Unknown value: " + value);
	}
	
}