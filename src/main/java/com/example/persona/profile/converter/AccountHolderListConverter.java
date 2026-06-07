package com.example.persona.profile.converter;

import com.example.persona.profile.dto.response.AccountHolderResponse;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Converter
public class AccountHolderListConverter implements AttributeConverter<List<AccountHolderResponse>, String> {

    private static final Logger log = LoggerFactory.getLogger(AccountHolderListConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<AccountHolderResponse> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JacksonException e) {
            log.error("Error converting account holder list to JSON string: {}", e.getMessage());
            throw new RuntimeException("Error converting account holder list to JSON string", e);
        }
    }

    @Override
    public List<AccountHolderResponse> convertToEntityAttribute(String dbData) {
        if (!StringUtils.hasText(dbData)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<>() {});
        } catch (JacksonException e) {
            log.error("Error converting JSON string to account holder list: {}", e.getMessage());
            throw new RuntimeException("Error converting JSON string to account holder list", e);
        }
    }
}
