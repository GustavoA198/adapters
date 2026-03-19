package co.com.clients.parent.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConstantField {

    public static final String ISO_DATE_PATTERN = "yyyy-MM-dd";
    public static final String ISO_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    public static final String ERROR_CHANNEL = "integrationExceptionInputChannel";
    public static final String RABBIT_MQ_ERROR_HANDLER = "rabbitMqErrorHandler";

    public static final String STATUS_OK = "200";
    public static final String MESSAGE_OK = "OperaciÃ³n exitosa";
    public static final String STATUS_PENDING = "202";
    public static final String MESSAGE_PENDING = "PROCESSING";
    public static final String STATUS_ERROR = "500";

    public static final String ACCESS_DENIED = "Access denied!";
	public static final String INVALID_REQUEST = "Invalid request";
	public static final String ERROR_MESSAGE_TEMPLATE = "message: %s %n requested uri: %s";
	public static final String LIST_JOIN_DELIMITER = ",";
	public static final String FIELD_ERROR_SEPARATOR = ": ";
}
