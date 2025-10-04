package com.sqli.medwork.exception;

/**
 * Exception thrown when a visit cannot be cancelled
 */
public class VisitCancellationException extends RuntimeException {
    
    public VisitCancellationException(String message) {
        super(message);
    }
    
    public VisitCancellationException(String message, Throwable cause) {
        super(message, cause);
    }
}
