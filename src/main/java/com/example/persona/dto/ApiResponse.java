package com.example.persona.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ApiResponse<T> {

    @JsonProperty("trace_id")
    private String traceId;

    private LocalDateTime timestamp;
    private String status = "success";
    private T data;
    private String message;

    public ApiResponse(T data) {
        this.data = data;
        this.traceId = MDC.get("traceId");
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(T data, String message) {
        this.data = data;
        this.message = message;
        this.traceId = MDC.get("traceId");
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setMessage(message);
        response.setStatus("error");
        response.setData(null);
        response.setTraceId(MDC.get("traceId"));
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}
