package com.sqli.medwork.exception;

import org.springframework.http.HttpStatus;

public class RoleRequiredException extends ApiException {
    public RoleRequiredException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "ROLE_REQUIRED");
    }
}