package com.sqli.medwork.exception;

import org.springframework.http.HttpStatus;

public class InvalidRoleException extends ApiException {
    public InvalidRoleException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_ROLE");
    }
}