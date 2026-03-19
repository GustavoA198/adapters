package co.com.clients.parent.utility;

import static co.com.clients.parent.utility.Utilities.isNullOrEmptyField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import co.com.clients.parent.exception.BusinessException;
import co.com.clients.parent.exception.SystemException;
import co.com.clients.parent.exception.ValidationException;
import co.com.clients.parent.exception.BackendException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// Utilidades para manejo y validaciÃ³n de errores en la capa parent
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UtilitiesErrors {

    /**
     * Lanza una excepciÃ³n si el campo estÃ¡ vacÃ­o o es nulo.
     * @param field Nombre del campo
     * @throws ValidationException si el campo es vacÃ­o o nulo
     */
    public static void throwEmptyOrNull(String field) throws BackendException {
        throw new ValidationException.FieldEmptyException(field);
    }

    /**
     * Lanza una excepciÃ³n si el campo es requerido y no estÃ¡ presente.
     * @param field Nombre del campo
     * @throws ValidationException si el campo es requerido y no estÃ¡ presente
     */
    public static void throwRequired(String field) throws BackendException {
        throw new ValidationException.FieldRequiredException(field);
    }

    /**
     * Lanza una excepciÃ³n si el campo no es vÃ¡lido.
     * @param field Nombre del campo
     * @throws ValidationException si el campo no es vÃ¡lido
     */
    public static void throwInvalid(String field) throws BackendException {
        throw new ValidationException.FieldNotValidException(field);
    }

    /**
     * Lanza una excepciÃ³n si el campo no cumple con el tamaÃ±o mÃ­nimo.
     * @param field Nombre del campo
     * @throws ValidationException si el campo no es vÃ¡lido
     */
    public static void throwMinLenght(String field, int size) throws BackendException {
        throw new ValidationException.FieldMinLengthException(field, size);
    }

    /**
     * Lanza una excepciÃ³n si el campo no cumple con el tamaÃ±o mÃ¡ximo.
     * @param field Nombre del campo
     * @throws ValidationException si el campo no es vÃ¡lido
     */
    public static void throwMaxLenght(String field, int size) throws BackendException {
        throw new ValidationException.FieldMaxLengthException(field, size);
    }

    /**
     * Lanza las excepciones producidas por los backend invocados.
     * @param code CÃ³digo de error
     * @param message Mensaje de error
     * @throws ValidationException si el valor no corresponde al catÃ¡logo
     */
    public static void throwBackendError(String code, String message) throws BackendException {
        throw new BusinessException.BackendException(code, message);
    }

    /**
     * Lanza una excepciÃ³n si el valor no existe.
     * @param field Nombre del campo
     * @throws ValidationException si el valor no corresponde al catÃ¡logo
     */
    public static void throwValueNotFound(String field) throws BackendException {
        throw new BusinessException.ValueNotFoundException(field);
    }

    /**
     * Lanza una excepciÃ³n si el valor no corresponde al catÃ¡logo esperado.
     * @param field Nombre del campo
     * @param catalog CatÃ¡logo esperado
     * @throws ValidationException si el valor no corresponde al catÃ¡logo
     */
    public static void throwCatalogNotMatch(String field, String catalog) throws BackendException {
        throw new BusinessException.ValueNotInCatalogException(field, catalog);
    }

    /**
     * Lanza una excepciÃ³n si la respuesta del servicio es nula.
     * @param serviceName Nombre del campo o servicio
     * @throws BusinessException si la respuesta es nula
     */
    public static void throwResponseServiceNull(String serviceName) throws BackendException {
        throw new SystemException.ResponseServiceNullException(serviceName);
    }

    /**
     * Lanza una excepciÃ³n si hay un error en la respuesta del servicio.
     * @param serviceName Nombre del campo o servicio
     * @throws BackendException si hay error en la respuesta
     */
    public static void throwResponseServiceError(String serviceName) throws BackendException {
    	throw new SystemException.ResponseServiceErrorException(serviceName);
    }

    /**
     * Lanza una excepciÃ³n si hay un error en la respuesta del servicio.
     * @param serviceName Nombre del campo o servicio
     * @throws BackendException si hay error en la respuesta
     */
    public static void throwResponseServiceMapping(String serviceName) throws BackendException {
    	throw new SystemException.ResponseServiceErrorMappingException(serviceName);
    }

    /**
     * Valida que la respuesta del cliente no sea nula o vacÃ­a.
     * @param response Respuesta del cliente
     * @param serviceName Nombre del servicio
     * @throws BusinessException si la respuesta es nula o vacÃ­a
     */
    public static void checkClientResponse(Object response, String serviceName) throws BackendException {
        if (isNullOrEmptyField(response)) {
        	throwResponseServiceNull(serviceName);
        }
    }

    /**
     * Valida que el valor no sea nulo ni blanco (para Strings).
     * @param value Valor a validar
     * @param fieldLabel Etiqueta del campo
     * @throws ValidationException si es nulo o blanco
     */
    public static void validateNullOrBlank(Object value, String fieldLabel) throws BackendException {
        if (value == null) {
        	throwEmptyOrNull(fieldLabel);
        }
        // Si es String, valida que no sea blanco
        if (value instanceof String string && string.isBlank()) {
        	throwEmptyOrNull(fieldLabel);
        }
    }

    /**
     * Valida que una lista no sea nula ni vacÃ­a.
     * @param list Lista a validar
     * @param fieldName Nombre del campo
     * @param <T> Tipo de la lista
     * @throws ValidationException si la lista es nula o vacÃ­a
     */
    public static <T> void validateNotEmptyList(List<T> list, String fieldName) throws BackendException {
        if (list == null || list.isEmpty()) {
        	throwRequired(fieldName);
        }
    }

    /**
     * Valida un campo String segÃºn si es requerido, formato y tamaÃ±o.
     * @param field Valor del campo
     * @param name Nombre del campo
     * @param isRequired Si es requerido
     * @param formatoRegex ExpresiÃ³n regular de formato
     * @param min TamaÃ±o mÃ­nimo
     * @param max TamaÃ±o mÃ¡ximo
     * @throws ValidationException si alguna validaciÃ³n falla
     */
	public static void validateField(String field, String name, boolean isRequired, String formatoRegex,
			Integer min, Integer max) throws BackendException {
		// Se valida si el campo es nulo o vacÃ­o
		if (field == null) {
			if (isRequired) {
				throwRequired(name);
			}
			return;
		}

        if(!isNullOrEmptyField(formatoRegex)) {
            // Se valida si el campo cumple con el regex
			if (!field.matches(formatoRegex)) {
				throwInvalid(name);
			}
		}

        if (!isNullOrEmptyField(min) && min > 0) {
			if (field.length() < min) {
				throwMinLenght(name, min);
			}
		}

        if (!isNullOrEmptyField(max) && max > 0) {
			if (field.length() > max) {
				throwMaxLenght(name, max);
			}
		}
	}

    /**
     * Valida un campo String segÃºn si es requerido y tamaÃ±o.
     * @param field Valor del campo
     * @param name Nombre del campo
     * @param isRequired Si es requerido
     * @param min TamaÃ±o mÃ­nimo
     * @param max TamaÃ±o mÃ¡ximo
     * @throws ValidationException si alguna validaciÃ³n falla
     */
    public static void validateField(String field, String name, boolean isRequired,
                                 Integer min, Integer max) throws BackendException {
        validateField(field, name, isRequired, null, min, max);
    }

    /**
     * Valida un campo String segÃºn si es requerido y formato.
     *
     * @param field        Valor del campo
     * @param name         Nombre del campo
     * @param isRequired   Si es requerido
     * @param formatoRegex ExpresiÃ³n regular de formato
     * @throws ValidationException si alguna validaciÃ³n falla
     */
    public static void validateField(String field, String name, boolean isRequired, String formatoRegex)
            throws BackendException {
        validateField(field, name, isRequired, formatoRegex, null, null);
    }

    /**
     * Valida si una fecha proporcionada cumple con el formato esperado y representa una fecha real.
     *
     * @param field  Fecha en formato texto a validar (por ejemplo, "29-02-2020").
     * @param format Formato de fecha esperado (por ejemplo, "dd-MM-yyyy").
     * @param name   Nombre del campo, usado para mensajes de error descriptivos.
     *
     * @throws ValidationException si alguna validaciÃ³n falla
     */
    public static void validateDate(String field, String format, String name) throws BackendException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        try {
            LocalDate.parse(field, formatter);
        } catch (DateTimeParseException e) {
            throwInvalid(name);
        }
    }
    
    /**
     * Lanza una excepciÃ³n si el campo no cumple con el tamaÃ±o mÃ­nimo.
     * @param field Nombre del campo
     * @throws ValidationException si el campo no es vÃ¡lido
     */
    public static void throwMinValue(String field, int size) throws BackendException {
        throw new ValidationException.FieldMinValueException(field, size);
    }

    /**
     * Lanza una excepciÃ³n si el campo no cumple con el tamaÃ±o mÃ¡ximo.
     * @param field Nombre del campo
     * @throws ValidationException si el campo no es vÃ¡lido
     */
    public static void throwMaxValue(String field, int size) throws BackendException {
        throw new ValidationException.FieldMaxValueException(field, size);
    }
}
