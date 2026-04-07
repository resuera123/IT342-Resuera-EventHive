package edu.cit.resuera.eventhive.adapter;

import java.security.Principal;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationAdapter {

    public String getEmail(OAuth2User oauthUser, Principal principal) {
        if (oauthUser != null) return oauthUser.getAttribute("email");
        if (principal != null) return principal.getName();
        throw new RuntimeException("Not authenticated");
    }

    public String getEmailOrNull(OAuth2User oauthUser, Principal principal) {
        if (oauthUser != null) return oauthUser.getAttribute("email");
        if (principal != null) return principal.getName();
        return null;
    }
}