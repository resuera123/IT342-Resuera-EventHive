package edu.cit.resuera.eventhive.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.cit.resuera.eventhive.entity.User;
import edu.cit.resuera.eventhive.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Update profile (firstname, lastname, email)
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {

        String currentEmail = resolveEmail(oauthUser, principal);
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newFirstname = body.get("firstname");
        String newLastname = body.get("lastname");
        String newEmail = body.get("email");

        if (newFirstname != null && !newFirstname.isBlank()) {
            user.setFirstname(newFirstname.trim());
        }
        if (newLastname != null && !newLastname.isBlank()) {
            user.setLastname(newLastname.trim());
        }
        if (newEmail != null && !newEmail.isBlank() && !newEmail.equals(currentEmail)) {
            if (userRepository.existsByEmail(newEmail)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email already in use"));
            }
            user.setEmail(newEmail.trim());
        }

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "id", user.getId(),
                "firstname", user.getFirstname(),
                "lastname", user.getLastname(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "createdAt", user.getCreatedAt().toString()
        ));
    }

    // Change password
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {

        String email = resolveEmail(oauthUser, principal);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (user.getPasswordHash() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "OAuth accounts cannot change password here"));
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Current password is incorrect"));
        }

        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("message", "New password must be at least 8 characters"));
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    private String resolveEmail(OAuth2User oauthUser, Principal principal) {
        if (oauthUser != null) return oauthUser.getAttribute("email");
        if (principal != null) return principal.getName();
        throw new RuntimeException("Not authenticated");
    }

    @PostMapping(value = "/profile-pic", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadProfilePic(
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {
 
        String email = resolveEmail(oauthUser, principal);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        try {
            String uploadDir = "uploads/profiles/";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }
            String filename = user.getId() + "_" + System.currentTimeMillis() + "_" + image.getOriginalFilename();
            java.nio.file.Files.copy(image.getInputStream(), uploadPath.resolve(filename),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            String url = "/" + uploadDir + filename;
 
            user.setProfilePicUrl(url);
            userRepository.save(user);
 
            return ResponseEntity.ok(Map.of("profilePicUrl", url));
        } catch (java.io.IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Upload failed"));
        }
    }
 
    // Get profile pic URL
    @GetMapping("/profile-pic")
    public ResponseEntity<?> getProfilePic(
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {
        String email = resolveEmail(oauthUser, principal);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(Map.of("profilePicUrl", user.getProfilePicUrl() != null ? user.getProfilePicUrl() : ""));
    }
}