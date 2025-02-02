package com.leoluca.urlshortener.api.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.mapping.Document;
import org.bson.types.ObjectId;
import java.util.Date;

@Document(collection = "users")
public class User {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId userId; // MongoDB ObjectId

    private String email;
    private Date createdAt;

    // Empty constructor for Spring Data
    public User() {}

    // Constructor for new user creation
    public User(String email) {
        this.userId = new ObjectId(); // Automatically generate ObjectId
        this.email = email;
        this.createdAt = new Date();
    }

    // Getters and setters
    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}