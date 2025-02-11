package com.leoluca.urlshortener.api.url.exception;

public class UrlRetrievalException extends RuntimeException {
    public UrlRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}