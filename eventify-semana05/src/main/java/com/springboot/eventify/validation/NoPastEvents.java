package com.springboot.eventify.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = NoPastEventsValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoPastEvents {

    String message() default "La fecha del evento no puede ser anterior a la fecha actual";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

