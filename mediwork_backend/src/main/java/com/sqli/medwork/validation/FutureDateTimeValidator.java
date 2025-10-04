package com.sqli.medwork.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

/**
 * Validator for FutureDateTime annotation
 * Provides more flexible validation than standard @Future
 */
public class FutureDateTimeValidator implements ConstraintValidator<FutureDateTime, LocalDateTime> {

    private int toleranceMinutes;

    @Override
    public void initialize(FutureDateTime constraintAnnotation) {
        this.toleranceMinutes = constraintAnnotation.toleranceMinutes();
    }

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        // Null values are considered valid (use @NotNull for null checking)
        if (value == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime toleranceTime = now.minusMinutes(toleranceMinutes);
        
        // Value is valid if it's after the tolerance time
        return value.isAfter(toleranceTime);
    }
}
