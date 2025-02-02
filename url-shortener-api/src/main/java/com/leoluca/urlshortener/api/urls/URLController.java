package com.leoluca.urlshortener.api.urls;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
public class URLController {

    private final URLService urlService;

    public URLController(URLService urlService) {
        this.urlService = urlService;
    }

   /**
     * GET / - Retrieve all URLs.
     *
     * @return A list of all URLs.
     */
    @GetMapping
    public ResponseEntity<Iterable<URL>> getAllUrls() {
        Iterable<URL> urls = urlService.getAllUrls();
        return ResponseEntity.ok(urls);
    }

    /**
     * POST /shorten - Shortens a long URL.
     *
     * @param request The request containing the long URL.
     * @return The generated short code.
     */
    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@Valid @RequestBody ShortenUrlRequest request) {
        if (!isValidUrl(request.getLongUrl())) {
            return ResponseEntity.badRequest().body("Invalid URL format. Please provide a valid HTTP/HTTPS URL.");
        }

        // Pass the long URL and user ID to the URL service
        String shortCode = urlService.saveShortUrl(request.getLongUrl(), request.getUserId());

        return ResponseEntity.ok("swisscom.com/" +shortCode);
    }

    /**
     * Helper method to validate URL format using a regex.
     *
     * @param url The URL to validate.
     * @return True if the URL is valid, false otherwise.
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        String regex = "^(http|https)://.*$"; // thanks google
        return url.matches(regex);
    }

    /**
     * GET /{shortCode} - Resolves a short code to its long URL.
     *
     * @param shortCode The short code from the request path.
     * @return A redirect to the long URL.
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> resolveShortUrl(@PathVariable String shortCode) {
        try {
            String longUrl = urlService.resolveShortCode(shortCode);

            // Return an HTTP 302 redirect to the original URL
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", longUrl)
                    .build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}