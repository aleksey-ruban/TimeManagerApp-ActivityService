package com.alekseyruban.timemanagerapp.activity_service.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.ZoneId;

public class TimeZoneValidator implements ConstraintValidator<ValidTimeZone, String> {

    private boolean allowEmpty;

    @Override
    public void initialize(ValidTimeZone annotation) {
        this.allowEmpty = annotation.allowEmpty();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (allowEmpty) {
            if (value == null || value.isBlank()) {
                return true;
            }
        }

        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            ZoneId.of(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}