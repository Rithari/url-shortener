package com.leoluca.urlshortener.api.url.exception;

public class UrlCreationException extends RuntimeException {
    public UrlCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}