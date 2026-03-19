package co.com.clients.parent.exception;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppException implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;
  
  @Schema(description = "CÃ³digo del mensaje de error", type = "String", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty("errorCode")
  private String code;
  @Schema(description = "Mensaje de error", type = "String", requiredMode = RequiredMode.REQUIRED)
  @JsonProperty("errorMessage")
  private String message;
  private ErrorType errorType;
  
}
