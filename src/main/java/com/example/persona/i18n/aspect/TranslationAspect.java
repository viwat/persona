package com.example.persona.i18n.aspect;

import com.example.persona.i18n.annotation.Translatable;
import com.example.persona.i18n.service.TranslationService;
import java.lang.reflect.Field;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class TranslationAspect {

    private final TranslationService translationService;

    @AfterReturning(
            pointcut =
                    "execution(* com.example.persona..*(..)) && !within(@org.springframework.context.annotation.Configuration *)",
            returning = "result")
    public void processTranslations(Object result) {
        if (result == null) {
            return;
        }

        try {
            processObject(result);
        } catch (Exception e) {
            log.error(
                    "Error processing translations for object: {}",
                    result.getClass().getName(),
                    e);
        }
    }

    private void processObject(Object obj) {
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            Translatable annotation = field.getAnnotation(Translatable.class);
            if (annotation != null) {
                field.setAccessible(true);
                String translationCode = annotation.value();
                try {
                    String translatedValue = translationService.getTranslation(translationCode);
                    if (translatedValue != null) {
                        field.set(obj, translatedValue);
                    }
                } catch (Exception e) {
                    log.error("Error translating field: {} with code: {}", field.getName(), translationCode, e);
                }
            }
        }
    }
}
