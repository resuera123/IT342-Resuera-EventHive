package edu.cit.resuera.eventhive.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import edu.cit.resuera.eventhive.dto.AuthResponse;
import edu.cit.resuera.eventhive.dto.LoginRequest;
import edu.cit.resuera.eventhive.dto.RegisterRequest;
import edu.cit.resuera.eventhive.entity.User;
import edu.cit.resuera.eventhive.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse("Email already registered", null, null, null, null, null, null);
        }

        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new AuthResponse("User registered successfully", null, null, null, null, null, null);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            return new AuthResponse("Invalid credentials", null, null, null, null, null, null);
        }

        if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return new AuthResponse("Login successful", user.getId(), user.getFirstname(), user.getLastname(), user.getEmail(), user.getRole(), user.getCreatedAt());
        }

        return new AuthResponse("Invalid credentials", null, null, null, null, null, null);
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
                user.getCreatedAt()
            ))
            .orElse(new AuthResponse("User not found", null, null, null, null, null, null));
    }
}