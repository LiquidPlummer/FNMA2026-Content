package com.revature.spring_starter.services;

import com.revature.spring_starter.dtos.RegisterRequest;
import com.revature.spring_starter.models.User;
import com.revature.spring_starter.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Account creation and credential checking. Knows nothing about JWTs or cookies -
 * that is the controller's problem. This class only answers "is this really them?"
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create an account. Returns empty if the username is already taken.
     *
     * The plaintext password never reaches the database - we hash it on the way
     * in and the original is discarded when this method returns.
     */
    public Optional<User> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return Optional.empty();
        }

        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.roleOrDefault());

        return Optional.of(userRepository.save(user));
    }

    /**
     * Check a username/password pair. Returns empty for BOTH "no such user" and
     * "wrong password", on purpose - telling the caller which one it was hands an
     * attacker a way to discover valid usernames.
     */
    public Optional<User> authenticate(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                // matches() re-hashes the raw password with the salt baked into
                // the stored hash and compares. You can never just compare
                // encode(raw) to the stored value - BCrypt salts every call, so
                // the same password hashes to a different string every time.
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }
}
