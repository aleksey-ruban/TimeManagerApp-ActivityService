package com.alekseyruban.timemanagerapp.activity_service.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = TimeZoneValidator.class)
public @interface ValidTimeZone {

    String message() default "Invalid IANA time zone";

    boolean allowEmpty() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}