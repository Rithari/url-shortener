package com.leoluca.urlshortener.api.user;

import org.springframework.stereotype.Service;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a new user with the given email.
     *
     * @param email The email of the user to create.
     * @return The created User object.
     */
    public User createUser(String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with email " + email + " already exists.");
        }

        User user = new User(email);
        user.setUserId(new ObjectId());

        return userRepository.save(user);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return The User object if found.
     */
    public User getUserById(ObjectId userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found."));
    }

    /**
     * Retrieves a user by their email.
     *
     * @param email The email of the user to retrieve.
     * @return The User object if found.
     */
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Retrieves all users in the database.
     *
     * @return A list of all User objects.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}