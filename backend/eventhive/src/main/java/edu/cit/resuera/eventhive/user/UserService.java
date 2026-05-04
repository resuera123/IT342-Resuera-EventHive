package edu.cit.resuera.eventhive.user;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<String, Object> updateProfile(String currentEmail, Map<String, String> body) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (body.get("firstname") != null) user.setFirstname(body.get("firstname"));
        if (body.get("lastname") != null) user.setLastname(body.get("lastname"));
        if (body.get("email") != null) user.setEmail(body.get("email"));

        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("firstname", user.getFirstname());
        response.put("lastname", user.getLastname());
        response.put("email", user.getEmail());
        return response;
    }

    public Map<String, String> changePassword(String currentEmail, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return Map.of("message", "Password updated successfully");
    }

    public Map<String, String> uploadProfilePic(String email, MultipartFile image) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String uploadDir = "uploads/profiles/";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String filename = user.getId() + "_" + System.currentTimeMillis() + "_" + image.getOriginalFilename();
        Files.copy(image.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        String url = "/" + uploadDir + filename;

        user.setProfilePicUrl(url);
        userRepository.save(user);

        return Map.of("profilePicUrl", url);
    }

    public Map<String, String> getProfilePic(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return Map.of("profilePicUrl", user.getProfilePicUrl() != null ? user.getProfilePicUrl() : "");
    }
}