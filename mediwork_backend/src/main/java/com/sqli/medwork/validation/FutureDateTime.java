package com.sqli.medwork.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for future datetime validation
 * More flexible than @Future - allows current time with small tolerance
 */
@Documented
@Constraint(validatedBy = FutureDateTimeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FutureDateTime {
    
    String message() default "La date et heure doivent être dans le futur";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Tolerance in minutes for considering a datetime as "future"
     * Default is 5 minutes to handle clock differences and processing time
     */
    int toleranceMinutes() default 5;
}
