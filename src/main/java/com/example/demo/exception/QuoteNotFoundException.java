package com.example.demo.exception;

public class QuoteNotFoundException extends RuntimeException {
    public QuoteNotFoundException(Long id) {
        super("Quote dengan id " + id + " tidak ditemukan di DummyJSON");
    }
}

