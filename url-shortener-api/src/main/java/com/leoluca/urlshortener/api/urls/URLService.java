package com.leoluca.urlshortener.api.urls;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class URLService {

    private final URLRepository urlRepository;

    private static final String BASE62_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public URLService(URLRepository urlRepository) {
        this.urlRepository = urlRepository;
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
        Optional<URL> url = urlRepository.findByShortCode(shortCode);

        if (url.isPresent()) {
            return url.get().getLongUrl();
        }

        throw new RuntimeException("Short code not found: " + shortCode);
    }

    /**
     * Generates a unique 7-character alphanumeric short code using Base62 encoding.
     *
     * @return A randomly generated short code.
     */
    private String encodeURL() {
        Random random = new Random();
        StringBuilder shortCode;

        while (true) {
            // Generate a random 7-character string
            shortCode = new StringBuilder();
            for (int i = 0; i < 7; i++) {
                int index = random.nextInt(BASE62_CHARACTERS.length());
                shortCode.append(BASE62_CHARACTERS.charAt(index));
            }

            // Ensure the generated code doesn't already exist in the database
            if (urlRepository.findByShortCode(shortCode.toString()).isEmpty()) {
                break;
            }
        }

        return shortCode.toString();
    }

    public List<URL> getUrlsByUserId(ObjectId userId) {
        return urlRepository.findByUserId(userId);
    }

    public Iterable<URL> getAllUrls() {
        return urlRepository.findAll();
    }
}