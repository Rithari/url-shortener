package com.leoluca.urlshortener.api.url;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;
import java.util.Optional;

public interface URLRepository extends MongoRepository<URL, String> {

    Optional<URL> findByShortCode(String shortCode);

    Optional<URL> findByLongUrl(String longUrl);

    List<URL> findByUserId(ObjectId userId);

    List<URL> findTop10ByOrderByHitCountDesc();

    @Query("{ 'shortCode': ?0 }")
    @Update("{ '$inc': { 'hitCount': 1 } }")
    void incrementHitCount(String shortCode);
}