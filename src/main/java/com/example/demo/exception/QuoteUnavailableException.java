package com.example.demo.exception;

public class QuoteUnavailableException extends RuntimeException {
    public QuoteUnavailableException(String message) {
        super(message);
    }
}
