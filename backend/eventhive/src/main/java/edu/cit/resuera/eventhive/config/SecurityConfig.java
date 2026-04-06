package edu.cit.resuera.eventhive.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:5173"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            .securityContext(sc -> sc.securityContextRepository(securityContextRepository()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                    .requestMatchers("/api/events/registered", "/api/events/my-events").authenticated()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/events", "/api/events/**").permitAll()
                    .requestMatchers("/uploads/**").permitAll()
                    .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    String requestPath = request.getRequestURI();
                    if (requestPath.startsWith("/api/")) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    } else {
                        response.sendRedirect("http://localhost:5173/login");
                    }
                })
            )
            .oauth2Login(oauth -> oauth
                    .defaultSuccessUrl("/login-success", true)
            );

        return http.build();
    }
}