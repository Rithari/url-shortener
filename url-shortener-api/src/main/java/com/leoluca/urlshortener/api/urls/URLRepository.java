package com.leoluca.urlshortener.api.urls;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface URLRepository extends MongoRepository<URL, String> {

    Optional<URL> findByShortCode(String shortCode);

    // TODO: Caching with Redis
    Optional<URL> findByLongUrl(String longUrl);

    List<URL> findByUserId(ObjectId userId);
}