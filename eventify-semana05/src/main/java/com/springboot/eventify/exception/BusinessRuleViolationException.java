package com.springboot.eventify.exception;

public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String mensaje) {
        super(mensaje);
    }
}

