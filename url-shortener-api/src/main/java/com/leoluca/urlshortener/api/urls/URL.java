package com.leoluca.urlshortener.api.urls;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import org.bson.types.ObjectId;
import java.util.Date;

@Document(collection = "short_urls")
public class URL {

    @Id
    private String id; // mongodb ObjectId

    private String longUrl; // The original URL
    private String shortCode; // The generated 7-character short code
    private ObjectId userId; // objectId of the user who created this short URL
    private Date createdAt;

    public URL() {}

    public URL(String longUrl, String shortCode, ObjectId userId) {
        this.longUrl = longUrl;
        this.shortCode = shortCode;
        this.userId = userId;
        this.createdAt = new Date();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}