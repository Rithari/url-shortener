package com.leoluca.urlshortener.api.url;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;

public class ShortenUrlRequest {
    @NotBlank(message = "URL cannot be blank")
    private String longUrl;

    @NotNull(message = "User ID is required")
    private ObjectId userId;

    public String getLongUrl() {
        return longUrl;
    }

    public ObjectId getUserId() {
        return userId;
    }
}