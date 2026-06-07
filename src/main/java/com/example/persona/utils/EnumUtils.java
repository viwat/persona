package com.example.persona.utils;

import com.example.persona.exception.BusinessException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EnumUtils {
    public <E extends Enum<E>> E resolveEnum(Class<E> enumClass, String value, String errorMessage) {
        try {
            return Enum.valueOf(enumClass, value);
        } catch (Exception e) {
            throw new BusinessException(errorMessage + ": " + value);
        }
    }
}
