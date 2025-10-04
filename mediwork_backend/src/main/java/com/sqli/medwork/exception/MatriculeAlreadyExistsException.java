package com.sqli.medwork.exception;

import org.springframework.http.HttpStatus;

public class MatriculeAlreadyExistsException extends ApiException {
    public MatriculeAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT, "MATRICULE_ALREADY_EXISTS");
    }
}