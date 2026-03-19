package co.com.clients.parent.utility;

import static co.com.clients.parent.utility.ConstantField.ISO_DATE_PATTERN;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.context.i18n.LocaleContextHolder;

import co.com.clients.parent.exception.ValidationException;
import co.com.clients.parent.exception.BackendException;
import io.micrometer.observation.Observation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

// Clase de utilidades generales para operaciones comunes en la capa parent
@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utilities {

    /**
     * Convierte un objeto Date a String con el patrÃ³n ISO_DATE_PATTERN.
     *
     * @param date Fecha a convertir
     * @return String con la fecha formateada o null si hay error
     */
    public static String dateToString(Date date) {
        try {
            DateFormat formatter = new SimpleDateFormat(ISO_DATE_PATTERN);
            return formatter.format(date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Formatea una fecha Date a String con el patrÃ³n ISO_DATE_PATTERN.
     *
     * @param originalDate Fecha a formatear
     * @return String con la fecha formateada o null si hay error
     */
    public static String formatDateToString(Date originalDate) {
        try {
            if (originalDate == null) {
                return null;
            }
            DateFormat df = new SimpleDateFormat(ISO_DATE_PATTERN);
            return df.format(originalDate);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Cambia el formato de una fecha String de un patrÃ³n a otro.
     *
     * @param date          Fecha en String
     * @param inputPattern  PatrÃ³n de entrada
     * @param outputPattern PatrÃ³n de salida
     * @return Fecha formateada o null si hay error
     */
    public static String changeDateStringFormat(String date, String inputPattern, String outputPattern) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
            SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
            Date parseDate = inputFormat.parse(date);
            return outputFormat.format(parseDate);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtiene la fecha y hora actual en el formato especificado
     *
     * @param format String con el formato deseado para la fecha y hora
     * @return String con la fecha y hora actual en el formato especificado
     */
    public static String getDateTime(String format) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return LocalDateTime.now().format(formatter);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Valida que una lista no sea nula ni vacÃ­a.
     *
     * @param list      Lista a validar
     * @param fieldName Nombre del campo
     * @param <T>       Tipo de la lista
     * @throws ValidationException si la lista es nula o vacÃ­a
     */
    public static <T> void validateNotEmpty(List<T> list, String fieldName) throws BackendException {
        if (list == null || list.isEmpty()) {
        	UtilitiesErrors.throwRequired(fieldName);
        }
    }

    /**
     * Convierte un String a XMLGregorianCalendar usando un patrÃ³n de fecha.
     *
     * @param dateStr     Fecha en String
     * @param datePattern PatrÃ³n de la fecha
     * @return XMLGregorianCalendar o null si hay error
     */
    public static XMLGregorianCalendar convertToXMLGregorianCalendar(String dateStr, String datePattern) {
        try {
            // Definir el formato de la fecha
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePattern);

            // Parsear el String a LocalDateTime
            LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);

            // Convertir LocalDateTime a GregorianCalendar
            GregorianCalendar gregorianCalendar = GregorianCalendar
                    .from(localDateTime.atZone(TimeZone.getDefault().toZoneId()));

            // Convertir a XMLGregorianCalendar
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtiene el Locale actual del contexto de Spring.
     *
     * @return Locale actual
     */
    public static Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }


    /**
     * Retorna la fecha correspondiente a hace 'years' aÃ±os desde hoy.
     *
     * @param years AÃ±os a restar
     * @return LocalDate resultante
     */
    public static LocalDate getYearsAgo(int years) {
        return LocalDate.now().minusYears(years);
    }

    /**
     * Convierte una cadena en formato "yyyy-MM-dd" a LocalDate.
     *
     * @param birthDate Fecha de nacimiento en String
     * @return LocalDate o null si es invÃ¡lida
     */
    public static LocalDate parseBirthDate(String birthDate) throws BackendException {
        if (birthDate == null || birthDate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(birthDate, DateTimeFormatter.ofPattern(ISO_DATE_PATTERN));
        } catch (DateTimeParseException e) {
        	UtilitiesErrors.throwResponseServiceError("Formato de fecha de nacimiento invÃ¡lido: " + birthDate);
        	return null; // No deberÃ­a llegar aquÃ­, pero por si acaso
        }
    }

    /**
     * Valida si un campo es nulo o vacÃ­o (String o List).
     *
     * @param field Campo a validar
     * @return true si es nulo o vacÃ­o
     */
    public static boolean isNullOrEmptyField(Object field) {
        if (field == null) {
            return true;
        } else {
            if (field instanceof String) {
                return ((String) field).isBlank();
            }
            if (field instanceof List) {
                return ((List) field).isEmpty();
            }
        }

        return false;
    }

    /**
     * Valida si un campo es nulo.
     *
     * @param field Campo a validar
     * @return true si es nulo
     */
    public static boolean isNullField(Object field) {
        return field == null;
    }

    /**
     * Valida si un entero estÃ¡ en el rango [min, max].
     *
     * @param value Valor a validar
     * @param min   MÃ­nimo
     * @param max   MÃ¡ximo
     * @return true si estÃ¡ en el rango
     */
    public static boolean intInRange(Integer value, Integer min, Integer max) {
        return value >= min && value <= max;
    }

    /**
     * Concatena dos etiquetas de campo.
     *
     * @param base Etiqueta base
     * @param sub  Sub-etiqueta
     * @return Etiqueta concatenada
     */
    public static String fieldLabel(String base, String sub) {
        return String.format("%s %s", base, sub);
    }

    /**
     * Concatena segmentos de nombre de campo usando punto como separador.
     *
     * @param segments Segmentos de nombre
     * @return Nombre concatenado
     */
    public static String fieldLabelValidate(String... segments) {
        if (segments == null || segments.length == 0) {
            return "";
        }
        return Arrays.stream(segments)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("."));
    }

    /**
     * Valida si una cadena es numÃ©rica (entero o decimal).
     *
     * @param word Cadena a validar
     * @return true si es numÃ©rica
     */
    public static boolean isNumeric(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        return word.trim().matches("^\\d+(\\.\\d+)?$");
    }

    /**
     * Reemplaza todos los campos String nulos de un objeto por cadenas vacÃ­as.
     * Usa reflexiÃ³n para acceder a los campos.
     *
     * @param source Objeto a procesar
     * @return El mismo objeto con Strings nulos reemplazados, o null si source es null
     * @throws RuntimeException si hay error de acceso
     */
    public static Object replaceNullWithEmptyString(Object source) throws BackendException {
        if (source == null) {
            return null;
        }

        Field[] fields = source.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.getType().equals(String.class)) {
                field.setAccessible(true);
                try {
                    if (field.get(source) == null) {
                        field.set(source, "");
                    }
                } catch (IllegalAccessException e) {
                    UtilitiesErrors.throwResponseServiceError("Error accessing field: " + field.getName());
                }
            }
        }

        return source;
    }

    /**
     * Une mÃºltiples cadenas usando un delimitador, ignorando nulas o vacÃ­as.
     *
     * @param delimiter Delimitador
     * @param strings   Cadenas a unir
     * @return String resultante
     */
    public static String joinStrings(String delimiter, String... strings) {
        if (strings == null || strings.length == 0) {
            return "";
        }

        if (delimiter == null) {
            delimiter = "";
        }

        return Arrays.stream(strings)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(delimiter));
    }

    /**
     * Valida si una clase es primitiva o wrapper.
     *
     * @param type Clase a validar
     * @return true si es primitiva o wrapper
     */
    private static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive()
                || type == Boolean.class
                || type == Integer.class
                || type == Long.class
                || type == Short.class
                || type == Byte.class
                || type == Double.class
                || type == Float.class
                || type == Character.class;
    }

    /**
     * Codifica un String a Base64.
     *
     * @param texto Texto a codificar
     * @return String codificado en Base64
     */
    public static String stringABase64(String texto) {
        return Base64.getEncoder().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Envuelve un Runnable con un scope de observaciÃ³n (Micrometer).
     *
     * @param task        Runnable original
     * @param observation ObservaciÃ³n
     * @return Runnable envuelto
     */
    public static Runnable wrapRunnable(Runnable task, Observation observation) {
        if (observation == null) {
            return task;
        }

        return () -> {
            try (Observation.Scope scope = observation.openScope()) {
                task.run();
            }
        };
    }

    /**
     * Envuelve un Supplier con un scope de observaciÃ³n (Micrometer).
     *
     * @param supplier    Supplier original
     * @param observation ObservaciÃ³n
     * @return Supplier envuelto
     */
    public static <T> Supplier<T> wrapSupplier(Supplier<T> supplier, Observation observation) {
        if (observation == null) {
            return supplier;
        }

        return () -> {
            try (Observation.Scope scope = observation.openScope()) {
                return supplier.get();
            }
        };
    }

    /**
     * Formatea una fecha en el formato especificado. Ej: "2024-12-05 21:31:55" a 20241205
     *
     * @param value  Fecha a formatear
     * @param format Formato de salida
     * @return La fecha formateada como Integer, o 0 si hay error
     */
    public static Integer formatDate(String value, String format) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = inputFormat.parse(value);

            SimpleDateFormat outputFormat = new SimpleDateFormat(format);
            String formattedDate = outputFormat.format(date);

            return Integer.parseInt(formattedDate);
        } catch (Exception e) {
            return 0;
        }
    }
    
    public static Date formatDateFromString(String value, String format) throws BackendException {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(format);
            Date date = inputFormat.parse(value);

            return date;
        } catch (Exception e) {
        	UtilitiesErrors.throwResponseServiceMapping("Date");
        	return null; // Se agrega por compilaciÃ³n
        }
    }

    /**
     * Formatea una fecha de un formato a otro, por ejemplo de "yyyy/MM/dd" a "dd-MM-yyyy"
     *
     * @param originalDate Fecha original
     * @param inputFormat  Formato de entrada
     * @param outputFormat Formato de salida
     * @return String con la fecha formateada
     * @throws ParseException 
     */
    public static String formatDate(String originalDate, String inputFormat, String outputFormat) {
        if (originalDate == null || originalDate.isEmpty())
            return "";

        // Formato de entrada
        DateFormat formatoEntrada = new SimpleDateFormat(inputFormat);

        // Formato de salida
        DateFormat formatoSalida = new SimpleDateFormat(outputFormat);

        try {
        	// Parsear la fecha de entrada
        	Date fecha = formatoEntrada.parse(originalDate);
        	
        	// Formatear la fecha al nuevo formato
        	return formatoSalida.format(fecha);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return "";
		}
    }

    /**
     * Convierte un String a Integer.
     *
     * @param s String a convertir
     * @return Integer o null si s es null
     */
    public static Integer stringToInteger(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null; // Retorna null si no es un nÃºmero vÃ¡lido
        }
    }

    /**
     * Convierte un String a Long, retorna 0 si es nulo o invÃ¡lido.
     *
     * @param input String a convertir
     * @return Long resultante
     */
    public static Long stringToLong(String input) {
        if (input == null || input.trim().isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(input.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    
    /**
	 * Convierte un String a BigDecimal, retorna BigDecimal.ZERO si es nulo o invÃ¡lido.
	 *
	 * @param input String a convertir
	 * @return BigDecimal resultante
	 */
    public static BigDecimal stringToBigDecimal(String input) {
    	if (input == null || input.trim().isEmpty()) {
			return BigDecimal.ZERO;
		}
		try {
			return new BigDecimal(input.trim());
		} catch (NumberFormatException e) {
			return BigDecimal.ZERO;
		}
    }
}
