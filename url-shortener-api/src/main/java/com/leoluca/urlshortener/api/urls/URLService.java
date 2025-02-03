package com.leoluca.urlshortener.api.urls;

import org.bson.types.ObjectId;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class URLService {

    private final URLRepository urlRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BASE62_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public URLService(URLRepository urlRepository, RedisTemplate<String, String> redisTemplate) {
        this.urlRepository = urlRepository;
        this.redisTemplate = redisTemplate;
    }

    // This annotation guarantees that the method will be executed after the application context has been initialized
    // PostConstruct would typically run midway through the startup process.
    @EventListener(ApplicationReadyEvent.class)
    public void preloadCache() {
        System.out.println("Preloading cache...");
        List<URL> urls = urlRepository.findTop10ByOrderByHitCountDesc(); // Get the top 10 most accessed URLs
        for (URL url : urls) {
            String redisKey = "shortUrls::" + url.getShortCode();
            redisTemplate.opsForValue().set(redisKey, url.getLongUrl());
            // System.out.println("Added to cache: " + redisKey);
        }
    }

    /**
     * Saves a new URL in the database and returns the generated short code.
     * @param longUrl The long URL to shorten.
     * @param userId The ID of the user who created the short URL.
     * @return The generated short code.
     */
    public String saveShortUrl(String longUrl, ObjectId userId) {
        Optional<URL> existingUrl = urlRepository.findByLongUrl(longUrl);
        if (existingUrl.isPresent()) {
            return existingUrl.get().getShortCode();
        }

        String shortCode = encodeURL();

        // Create a new URL object with the given long URL and generated short code
        URL url = new URL();
        url.setLongUrl(longUrl);
        url.setShortCode(shortCode);
        url.setUserId(userId);
        url.setCreatedAt(new Date());

        urlRepository.save(url);

        return shortCode;
    }

    /**
     * Resolves a short code to its corresponding long URL.
     *
     * @param shortCode The short code to resolve.
     * @return The original long URL.
     */
    public String resolveShortCode(String shortCode) {
        System.out.println("Attempting to resolve shortCode: " + shortCode);

        String redisKey = "shortUrls::" + shortCode;
        String cachedLongUrl = redisTemplate.opsForValue().get(redisKey);

        if (cachedLongUrl != null) {
            System.out.println("Cache hit! Retrieved from Redis: " + cachedLongUrl);
        } else {
            System.out.println("Cache miss! Retrieving from MongoDB...");
            cachedLongUrl = urlRepository.findByShortCode(shortCode)
                    .map(URL::getLongUrl)
                    .orElseThrow(() -> new RuntimeException("Short code not found in MongoDB: " + shortCode));

            // Store it back in Redis for future use
            redisTemplate.opsForValue().set(redisKey, cachedLongUrl);
        }

        // hitCount is not a field in the URL class, but it is in the database so we use the repository to increment it
        urlRepository.incrementHitCount(shortCode);
        System.out.println("Incremented hit count for shortCode: " + shortCode);

        return cachedLongUrl;
    }

    /**
     * Generates a unique 7-character alphanumeric short code using Base62 encoding.
     *
     * @return A randomly generated short code.
     */
    private String encodeURL() {
        Random random = new Random();
        StringBuilder shortCode;

        do {
            // Generate a random 7-character string
            shortCode = new StringBuilder();
            for (int i = 0; i < 7; i++) {
                int index = random.nextInt(BASE62_CHARACTERS.length());
                shortCode.append(BASE62_CHARACTERS.charAt(index));
            }

            // Ensure the generated code doesn't already exist in the database
        } while (urlRepository.findByShortCode(shortCode.toString()).isPresent());

        return shortCode.toString();
    }

    public List<URL> getUrlsByUserId(ObjectId userId) {
        return urlRepository.findByUserId(userId);
    }

    public Iterable<URL> getAllUrls() {
        return urlRepository.findAll();
    }
}