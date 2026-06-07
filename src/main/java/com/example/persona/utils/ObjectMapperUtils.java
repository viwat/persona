package com.example.persona.utils;

import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
public class ObjectMapperUtils {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> List<T> readValues(List<String> contents, TypeReference<T> typeReference) {
        try {
            return contents.stream()
                    .map(object -> objectMapper.readValue(object, typeReference))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON", e);
        }
    }

    public <T> T readValue(String content, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(content, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON", e);
        }
    }

    public <T> T stringToObject(String content, Class<T> clazz) {
        try {
            return objectMapper.readValue(content, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON", e);
        }
    }

    public String writeValueAsString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }
}
