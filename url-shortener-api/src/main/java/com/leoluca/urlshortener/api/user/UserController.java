package com.leoluca.urlshortener.api.user;

import com.leoluca.urlshortener.api.url.URL;
import com.leoluca.urlshortener.api.url.URLService;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final URLService urlService;

    public UserController(UserService userService, URLService urlService) {
        this.userService = userService;
        this.urlService = urlService;
    }

    /**
     * POST /users - Create a new user.
     *
     * @param userRequest The request body containing the user's email.
     * @return The created user object.
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserRequest userRequest) {
        if (userRequest.getEmail() == null || userRequest.getEmail().isBlank() || isInvalidEmail(userRequest.getEmail())) {
            return ResponseEntity.badRequest().body(null);
        }

        User createdUser = userService.createUser(userRequest.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Helper method to validate email format using a regex.
     * @param email The email to validate.
     * @return True if the email is valid, false otherwise.
     */
    private boolean isInvalidEmail(String email) {
        if (email == null || email.isBlank()) {
            return true;
        }

        String regex = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$"; // thanks copilot
        return !email.matches(regex);
    }

    /**
     * GET /users/{userId} - Retrieve a user by ID.
     *
     * @param userId The ID of the user.
     * @return The user object if found.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable ObjectId userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * (Optional) GET /users - Retrieve all users.
     *
     * @return A list of all users.
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * POST /users/login - Authenticate a user by email.
     *
     * @param userRequest The request body containing the user's email.
     * @return The user object if found.
     */
    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@RequestBody UserRequest userRequest) {
        // Checking the email validity helps with database overhead
        if (userRequest.getEmail() == null || userRequest.getEmail().isBlank() || isInvalidEmail(userRequest.getEmail())) {
            return ResponseEntity.badRequest().body(null);
        }

        Optional<User> user = userService.findUserByEmail(userRequest.getEmail());

        // Return the user object as a mock "session"
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null));
    }

    /**
     * GET /users/{userId}/urls - Retrieve all URLs created by a user.
     *
     * @param userId The ID of the user.
     * @return A list of all URLs created by the user.
     */
    @GetMapping("/{userId}/urls")
    public ResponseEntity<List<URL>> getUserUrls(@PathVariable ObjectId userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        List<URL> urls = urlService.getUrlsByUserId(userId);
        return ResponseEntity.ok(urls);
    }
}