package com.example.persona.home.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GreetingResponse {
    private String greeting;
    private String userName;
    private String timeOfDay; // MORNING, AFTERNOON, EVENING, NIGHT
    private String fullGreeting; // Combined greeting with name
}
