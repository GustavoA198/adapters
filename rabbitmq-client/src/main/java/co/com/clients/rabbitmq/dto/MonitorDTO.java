package co.com.clients.rabbitmq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonitorDTO {
	
    @JsonProperty("Request")
    private String request;

    @JsonProperty("Response")
    private String response;

    @JsonProperty("Queue")
    private String queue;

}
