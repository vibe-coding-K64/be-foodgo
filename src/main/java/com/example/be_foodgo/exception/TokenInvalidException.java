package com.example.be_foodgo.exception;

import org.springframework.security.core.AuthenticationException;

public class TokenInvalidException extends AuthenticationException {

    public TokenInvalidException(String message) {
        super(message);
    }

    public TokenInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
