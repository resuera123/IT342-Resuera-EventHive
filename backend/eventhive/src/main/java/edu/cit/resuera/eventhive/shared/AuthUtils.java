package edu.cit.resuera.eventhive.shared;

import java.security.Principal;

import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Utility for resolving the authenticated user's email from either
 * an OAuth2 principal (Google login) or a session-based Principal (local login).
 * Used across all controllers to avoid duplication.
 */
public final class AuthUtils {

    private AuthUtils() {}

    public static String resolveEmail(OAuth2User oauthUser, Principal principal) {
        if (oauthUser != null) return oauthUser.getAttribute("email");
        if (principal != null) return principal.getName();
        throw new RuntimeException("Not authenticated");
    }

    public static String resolveEmailOrNull(OAuth2User oauthUser, Principal principal) {
        if (oauthUser != null) return oauthUser.getAttribute("email");
        if (principal != null) return principal.getName();
        return null;
    }
}