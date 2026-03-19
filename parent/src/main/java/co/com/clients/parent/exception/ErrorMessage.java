package co.com.clients.parent.exception;

import lombok.Getter;

/**
 * @author Zathura Code Generator Version 24.05 <a href="http://zathuracode.org/">...</a>
 * @generationDate 2024-07-11T16:45:15.952527
 */
@Getter
public enum ErrorMessage {

    // Validation Errors
	REQUEST_EMPTY("140001"),
    FIELD_REQUIRED("140002"),
    FIELD_NOT_VALID("140003"),
    FIELD_MIN_LENGTH("140004"),
    FIELD_MAX_LENGTH("140005"),
    FIELD_EMPTY("140006"),
    FIELD_MIN_VALUE("140007"),
    FIELD_MAX_VALUE("140008"),
    
    // Business Errors
    VALUE_NOT_FOUND("240401"),
    VALUE_NOT_IN_CATALOG("241201"),
    
    // System Errors
    RESPONSE_SERVICE_NULL("350301"),
    RESPONSE_SERVICE_ERROR("350001"),
    RESPONSE_SERVICE_ERROR_MAPPING("342201");

    private final String value;

    ErrorMessage(String value) {
        this.value = value;
    }
}