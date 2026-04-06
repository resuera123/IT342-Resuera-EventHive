package edu.cit.resuera.eventhive.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.resuera.eventhive.entity.Notification;
import edu.cit.resuera.eventhive.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Get all notifications for current user
    @GetMapping
    public List<Map<String, Object>> getNotifications(
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {
        String email = resolveEmail(oauthUser, principal);
        return notificationService.getUserNotifications(email)
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    // Get unread count
    @GetMapping("/unread-count")
    public Map<String, Integer> getUnreadCount(
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {
        String email = resolveEmail(oauthUser, principal);
        return Map.of("count", notificationService.getUnreadCount(email));
    }

    // Mark single notification as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {
        String email = resolveEmail(oauthUser, principal);
        notificationService.markAsRead(id, email);
        return ResponseEntity.ok(Map.of("message", "Marked as read"));
    }

    // Mark all as read
    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {
        String email = resolveEmail(oauthUser, principal);
        notificationService.markAllAsRead(email);
        return ResponseEntity.ok(Map.of("message", "All marked as read"));
    }

    private Map<String, Object> toMap(Notification n) {
        return Map.of(
                "id", n.getId(),
                "type", n.getType(),
                "title", n.getTitle(),
                "message", n.getMessage(),
                "read", n.isRead(),
                "eventId", n.getEventId() != null ? n.getEventId() : 0,
                "createdAt", n.getCreatedAt().toString()
        );
    }

    private String resolveEmail(OAuth2User oauthUser, Principal principal) {
        if (oauthUser != null) return oauthUser.getAttribute("email");
        if (principal != null) return principal.getName();
        throw new RuntimeException("Not authenticated");
    }
}