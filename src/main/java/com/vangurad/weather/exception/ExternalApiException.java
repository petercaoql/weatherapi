package com.vangurad.weather.exception;

public class ExternalApiException extends RuntimeException {
    public ExternalApiException(String message) {
        super(message);
    }
}
