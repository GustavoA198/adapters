package co.com.clients.parent.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * Interceptor de Feign para logging automÃ¡tico de requests y responses
 * Captura automÃ¡ticamente todas las llamadas HTTP de los clientes Feign
 */
@Slf4j
@Component
public class FeignLoggingInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // Log del request
        logRequest(template);
    }

    /**
     * Logs the request information including URL, method, headers and body
     *
     * @param template Request template from Feign
     */
    private void logRequest(RequestTemplate template) {
        try {
            String method = template.method();
            String relativeUrl = template.url();
            String feignClientName = template.feignTarget() != null ? template.feignTarget().name() : "UNKNOWN";

            // Get the full URL by combining base URL from target with relative URL
            String fullUrl = getFullUrl(template, relativeUrl);

            log.info("Llamando la operacion desde cliente Feign: {}", feignClientName);
            log.debug("Request enviado a la URL: {}, method={}, client={}", fullUrl, method, feignClientName);

            // Log headers
            if (template.headers() != null && !template.headers().isEmpty()) {
                log.debug("Request headers: {}", template.headers());
            }

            // Log request body
            if (template.body() != null && template.body().length > 0) {
                String requestBody = new String(template.body(), StandardCharsets.UTF_8);
                log.debug("Request body: {}", requestBody);

                // Generate CURL command with full URL
                String curlCommand = buildCurlCommand(fullUrl, method, requestBody, template.headers());
                log.debug("CURL equivalente: {}", curlCommand);
            }

        } catch (Exception e) {
            log.error("Error logging request", e);
        }
    }

    /**
     * Gets the full URL by combining base URL from Feign target with relative URL
     *
     * @param template    Request template
     * @param relativeUrl Relative URL from template
     * @return Full URL string
     */
    private String getFullUrl(RequestTemplate template, String relativeUrl) {
        if (template.feignTarget() != null) {
            String baseUrl = template.feignTarget().url();
            if (baseUrl != null && !baseUrl.isEmpty()) {
                // Remove trailing slash from base URL if present
                if (baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                }
                // Add leading slash to relative URL if not present
                if (!relativeUrl.startsWith("/")) {
                    relativeUrl = "/" + relativeUrl;
                }
                return baseUrl + relativeUrl;
            }
        }
        return relativeUrl; // Fallback to relative URL if no base URL found
    }


    /**
     * Builds a CURL command equivalent to the HTTP request
     *
     * @param url     URL endpoint
     * @param method  HTTP method
     * @param body    Request body
     * @param headers Request headers
     * @return CURL command string
     */
    private String buildCurlCommand(String url, String method, String body, Map<String, Collection<String>> headers) {
        StringBuilder curl = new StringBuilder();
        curl.append("curl -X ").append(method).append(" '").append(url).append("'");

        // Add headers (excluding Content-Length as it's not needed for CURL)
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, Collection<String>> entry : headers.entrySet()) {
                String headerName = entry.getKey();
                // Skip Content-Length header as it's automatically calculated by CURL
                if (!"Content-Length".equalsIgnoreCase(headerName)) {
                    for (String headerValue : entry.getValue()) {
                        curl.append(" -H '").append(headerName).append(": ").append(headerValue).append("'");
                    }
                }
            }
        }

        // Add body
        if (body != null && !body.isEmpty()) {
            curl.append(" -d '").append(body.replace("'", "'\"'\"'")).append("'");
        }

        return curl.toString();
    }
}
