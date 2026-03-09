package edu.cit.resuera.eventhive.controller;

import java.time.LocalDateTime;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.resuera.eventhive.entity.User;
import edu.cit.resuera.eventhive.repository.UserRepository;

@RestController
public class OAuthController {

    private final UserRepository userRepository;

    public OAuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/login-success")
    public String loginSuccess(@AuthenticationPrincipal OAuth2User oauthUser) {

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        String firstname = "";
        String lastname = "";

        if (name != null) {
            String[] parts = name.split(" ");

            firstname = parts[0];

            if (parts.length > 1) {
                lastname = parts[parts.length - 1];
            }
        }

        if (!userRepository.existsByEmail(email)) {

            User user = new User();
            user.setEmail(email);
            user.setFirstname(firstname);
            user.setLastname(lastname);
            user.setPasswordHash("GOOGLE_AUTH");
            user.setCreatedAt(LocalDateTime.now());

            userRepository.save(user);
        }

        return "Google Login Successful: " + email;
    }
}
 