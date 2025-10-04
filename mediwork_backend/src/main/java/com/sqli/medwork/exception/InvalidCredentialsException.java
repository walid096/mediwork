package com.sqli.medwork.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {
    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }
}