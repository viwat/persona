package com.example.persona.utils;

import com.example.persona.favorite.dto.UserFavoriteCreateRequest;
import com.example.persona.favorite.model.UserFavorite;
import java.util.Map;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

@Component
public class ConfigValueUtils {
    @AfterMapping
    public void mapAttributes(UserFavoriteCreateRequest request, @MappingTarget UserFavorite favorite) {
        if (request.getAttributes() == null) return;

        Map<String, String> attrs = request.getAttributes();

        setIfPresent(favorite::setConfigValue1, attrs, "config_value1");
        setIfPresent(favorite::setConfigValue2, attrs, "config_value2");
        setIfPresent(favorite::setConfigValue3, attrs, "config_value3");
        setIfPresent(favorite::setConfigValue4, attrs, "config_value4");
        setIfPresent(favorite::setConfigValue5, attrs, "config_value5");
        setIfPresent(favorite::setConfigValue6, attrs, "config_value6");
        setIfPresent(favorite::setConfigValue7, attrs, "config_value7");
        setIfPresent(favorite::setConfigValue8, attrs, "config_value8");
        setIfPresent(favorite::setConfigValue9, attrs, "config_value9");
        setIfPresent(favorite::setConfigValue10, attrs, "config_value10");
    }

    private void setIfPresent(java.util.function.Consumer<String> setter, Map<String, String> attributes, String key) {
        if (attributes.containsKey(key)) {
            setter.accept(attributes.get(key));
        }
    }
}
