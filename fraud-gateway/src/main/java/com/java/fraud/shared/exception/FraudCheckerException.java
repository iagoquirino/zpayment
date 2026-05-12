package com.java.fraud.shared.exception;

public class FraudCheckerException extends RuntimeException {
    public FraudCheckerException(Exception message) {
        super(message);
    }
}
