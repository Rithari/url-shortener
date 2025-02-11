package com.leoluca.urlshortener.api.url;

import com.leoluca.urlshortener.api.url.exception.*;
import org.bson.types.ObjectId;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class URLService {

    private static final Logger logger = LoggerFactory.getLogger(URLService.class);

    private final URLRepository urlRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BASE62_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public URLService(URLRepository urlRepository, RedisTemplate<String, String> redisTemplate) {
        this.urlRepository = urlRepository;
        this.redisTemplate = redisTemplate;
    }

    // This annotation guarantees that the method will be executed after the application context has been initialized
    // PostConstruct would typically run midway through the startup process and therefore may cause issues.
    @EventListener(ApplicationReadyEvent.class)
    public void preloadCache() {
        logger.info("Preloading cache with top 10 most clicked URLs...");
        try {
            List<URL> urls = urlRepository.findTop10ByOrderByHitCountDesc(); // Get the top 10 most clicked URLs
            for (URL url : urls) {
                String redisKey = "shortUrls::" + url.getShortCode();
                redisTemplate.opsForValue().set(redisKey, url.getLongUrl());
                logger.info("Cached URL: {}", redisKey);
            }
        } catch (Exception e) {
            logger.error("Failed to preload cache: {}", e.getMessage(), e);
        }
    }

    /**
     * Saves a new URL in the database and returns the generated short code.
     * @param longUrl The long URL to shorten.
     * @param userId The ID of the user who created the short URL.
     * @return The generated short code.
     */
    public String saveShortUrl(String longUrl, ObjectId userId) {
        try {
            // Validate URL before processing
            if (!isValidUrl(longUrl)) {
                throw new InvalidUrlException("Invalid URL format. Please provide a valid HTTP/HTTPS URL.");
            }

            Optional<URL> existingUrl = urlRepository.findByLongUrl(longUrl);
            if (existingUrl.isPresent()) {
                return existingUrl.get().getShortCode();
            }

            // Create the short URL object
            String shortCode = encodeURL();
            URL url = new URL();
            url.setLongUrl(longUrl);
            url.setShortCode(shortCode);
            url.setUserId(userId);
            url.setCreatedAt(new Date());

            urlRepository.save(url);
            logger.info("Created short URL: {} -> {}", shortCode, longUrl);
            return shortCode;
        } catch (InvalidUrlException e) {
            logger.warn("URL validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error saving short URL: {}", e.getMessage(), e);
            throw new UrlCreationException("Could not shorten the URL", e);
        }
    }

    /**
     * Validates the format of a URL.
     * @param url The URL to validate.
     * @return True if the URL is valid, false otherwise.
     */
    private boolean isValidUrl(String url) {
        String regex = "^(http|https)://.*$"; // thanks google
        return url != null && !url.isBlank() && url.matches(regex);
    }

    /**
     * Resolves a short code to its corresponding long URL.
     *
     * @param shortCode The short code to resolve.
     * @return The original long URL.
     */
    public String resolveShortCode(String shortCode) {
        logger.info("Resolving short code: {}", shortCode);
        String redisKey = "shortUrls::" + shortCode;

        try {
            // Check cache first
            String cachedLongUrl = redisTemplate.opsForValue().get(redisKey);
            if (cachedLongUrl != null) {
                logger.info("Cache hit for {}", shortCode);
                return cachedLongUrl;
            }

            // Retrieve from database
            logger.info("Cache miss for {}. Querying MongoDB...", shortCode);
            cachedLongUrl = urlRepository.findByShortCode(shortCode)
                    .map(URL::getLongUrl)
                    .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + shortCode));

            // Store in cache
            redisTemplate.opsForValue().set(redisKey, cachedLongUrl); // opsforValue is basically the SET command
            logger.info("Cached {} in Redis", shortCode);

            // Increment hit count
            urlRepository.incrementHitCount(shortCode);
            logger.info("Incremented hit count for {}", shortCode);

            return cachedLongUrl;
        } catch (UrlNotFoundException e) {
            logger.warn("URL not found: {}", shortCode);
            throw e;
        } catch (Exception e) {
            logger.error("Error resolving short code {}: {}", shortCode, e.getMessage(), e);
            throw new UrlResolutionException("Error resolving short URL: " + shortCode, e);
        }
    }

    /**
     * Generates a unique 7-character short code for a URL.
     *
     * @return The generated short code.
     */
    private String encodeURL() {
        Random random = new Random();
        StringBuilder shortCode;

        try {
            do {
                shortCode = new StringBuilder();
                for (int i = 0; i < 7; i++) {
                    int index = random.nextInt(BASE62_CHARACTERS.length());
                    shortCode.append(BASE62_CHARACTERS.charAt(index));
                }
            } while (urlRepository.findByShortCode(shortCode.toString()).isPresent());
            // Keep generating short codes until we find one that doesn't already exist

            return shortCode.toString();
        } catch (Exception e) {
            logger.error("Error generating short code: {}", e.getMessage(), e);
            throw new UrlCreationException("Error generating short code", e);
        }
    }

    /**
     * Retrieves all URLs created by a specific user.
     *
     * @param userId The ID of the user to retrieve URLs for.
     * @return A list of URLs created by the user.
     */
    public List<URL> getUrlsByUserId(ObjectId userId) {
        try {
            return urlRepository.findByUserId(userId);
        } catch (Exception e) {
            logger.error("Error retrieving URLs for user {}: {}", userId, e.getMessage(), e);
            throw new UrlRetrievalException("Error retrieving URLs for user", e);
        }
    }

    /**
     * Retrieves all URLs in the database.
     *
     * @return A list of all URLs.
     */
    public Iterable<URL> getAllUrls() {
        try {
            return urlRepository.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving all URLs: {}", e.getMessage(), e);
            throw new UrlRetrievalException("Error retrieving all URLs", e);
        }
    }
}