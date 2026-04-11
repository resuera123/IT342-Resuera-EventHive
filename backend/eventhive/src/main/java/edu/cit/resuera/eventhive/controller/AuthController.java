package edu.cit.resuera.eventhive.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.resuera.eventhive.dto.AuthResponse;
import edu.cit.resuera.eventhive.dto.LoginRequest;
import edu.cit.resuera.eventhive.dto.RegisterRequest;
import edu.cit.resuera.eventhive.service.AuthService;
import edu.cit.resuera.eventhive.entity.User;
import edu.cit.resuera.eventhive.repository.UserRepository;

import java.security.Principal;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.context.SecurityContextRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, SecurityContextRepository securityContextRepository, UserRepository userRepository) {
        this.authService = authService;
        this.securityContextRepository = securityContextRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request,
                              HttpServletRequest httpRequest,
                              HttpServletResponse httpResponse) {
        AuthResponse response = authService.login(request);

        // If login was successful (id is not null), create a Spring Security session
        if (response.getId() != null) {
            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                    response.getEmail(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + response.getRole().toUpperCase()))
                );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);
        }

        return response;
    }

    @GetMapping("/me")
    public AuthResponse getCurrentUser(@AuthenticationPrincipal OAuth2User oauthUser, Principal principal) {

        String email = null;

        if (oauthUser != null) {
            email = oauthUser.getAttribute("email");
        } else if (principal != null) {
            email = principal.getName();
        }

        if (email == null) {
            return new AuthResponse("Not authenticated", null, null, null, null, null, null, null);
        }

        return authService.getCurrentUser(email);
    }

    @PostMapping("/google-mobile")
    public AuthResponse googleMobileLogin(
            @RequestBody java.util.Map<String, String> body,
            jakarta.servlet.http.HttpServletRequest httpRequest,
            jakarta.servlet.http.HttpServletResponse httpResponse) {
 
        String email = body.get("email");
        String firstname = body.get("firstname");
        String lastname = body.get("lastname");
 
        AuthResponse response = authService.googleMobileLogin(email, firstname, lastname);
 
        if (response.getId() != null) {
            var authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    response.getEmail(), null,
                    java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                            "ROLE_" + response.getRole().toUpperCase()))
            );
            var context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            org.springframework.security.core.context.SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);
        }
 
        return response;
    }
}