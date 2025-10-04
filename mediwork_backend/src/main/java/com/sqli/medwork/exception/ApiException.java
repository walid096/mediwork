package com.sqli.medwork.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

   /* public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = "API_ERROR";
    }
    */

    public ApiException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
