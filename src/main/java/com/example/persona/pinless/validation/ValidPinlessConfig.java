package com.example.persona.pinless.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PinlessConfigValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPinlessConfig {
    String message() default "Invalid pinless config";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
