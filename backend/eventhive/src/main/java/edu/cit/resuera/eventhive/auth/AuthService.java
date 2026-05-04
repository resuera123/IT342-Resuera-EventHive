package edu.cit.resuera.eventhive.auth;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import edu.cit.resuera.eventhive.auth.dto.AuthResponse;
import edu.cit.resuera.eventhive.auth.dto.LoginRequest;
import edu.cit.resuera.eventhive.auth.dto.RegisterRequest;
import edu.cit.resuera.eventhive.user.User;
import edu.cit.resuera.eventhive.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse("Email already registered", null, null, null, null, null, null, null);
        }

        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new AuthResponse("User registered successfully", null, null, null, null, null, null, null);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            return new AuthResponse("Invalid credentials", null, null, null, null, null, null, null);
        }

        if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return new AuthResponse("Login successful", user.getId(), user.getFirstname(),
                    user.getLastname(), user.getEmail(), user.getRole(), user.getCreatedAt(),
                    user.getProfilePicUrl());
        }

        return new AuthResponse("Invalid credentials", null, null, null, null, null, null, null);
    }

    public AuthResponse getCurrentUser(String email) {
        return userRepository.findByEmail(email)
            .map(user -> new AuthResponse(
                "User profile retrieved",
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getProfilePicUrl()
            ))
            .orElse(new AuthResponse("User not found", null, null, null, null, null, null, null));
    }

    public AuthResponse googleMobileLogin(String email, String firstname, String lastname) {
        if (email == null || email.isEmpty()) {
            return new AuthResponse("Missing email", null, null, null, null, null, null, null);
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFirstname(firstname != null ? firstname : "");
            user.setLastname(lastname != null ? lastname : "");
            user.setPasswordHash("GOOGLE_AUTH");
            user.setRole("participant");
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        return new AuthResponse("Login successful", user.getId(), user.getFirstname(),
                user.getLastname(), user.getEmail(), user.getRole(), user.getCreatedAt(),
                user.getProfilePicUrl());
    }
}