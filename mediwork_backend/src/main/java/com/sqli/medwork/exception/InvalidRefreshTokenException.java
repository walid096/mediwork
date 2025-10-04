package com.sqli.medwork.exception;
/**
 * Exception thrown when a refresh token is invalid.
 * This could be due to the token being expired, malformed, or not found in the database.
 */
public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super();
    }

    public InvalidRefreshTokenException(String message) {
        super(message);
    }

    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}