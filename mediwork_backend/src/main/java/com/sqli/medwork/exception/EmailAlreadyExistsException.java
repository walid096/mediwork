package com.sqli.medwork.exception;


import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends ApiException {
    public EmailAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS");
    }
}