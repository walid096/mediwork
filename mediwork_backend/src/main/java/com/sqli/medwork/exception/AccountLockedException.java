package com.sqli.medwork.exception;


import org.springframework.http.HttpStatus;

public class AccountLockedException extends ApiException {
    public AccountLockedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "ACCOUNT_LOCKED");
    }
}