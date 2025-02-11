package com.leoluca.urlshortener.api.url.exception;

public class UrlResolutionException extends RuntimeException {
    public UrlResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}