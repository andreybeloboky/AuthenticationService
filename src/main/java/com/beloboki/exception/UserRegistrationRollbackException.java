package com.beloboki.exception;

public class UserRegistrationRollbackException extends RuntimeException {
    public UserRegistrationRollbackException(String message, Throwable cause) {
        super(message, cause);
    }
}
