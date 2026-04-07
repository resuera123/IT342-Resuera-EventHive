package edu.cit.resuera.eventhive.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.cit.resuera.eventhive.dto.EventRequest;
import edu.cit.resuera.eventhive.dto.EventResponse;
import edu.cit.resuera.eventhive.entity.EventStatus;
import edu.cit.resuera.eventhive.service.EventService;
import edu.cit.resuera.eventhive.adapter.AuthenticationAdapter;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final AuthenticationAdapter authAdapter;

    public EventController(EventService eventService, AuthenticationAdapter authAdapter) {
        this.eventService = eventService;
        this.authAdapter = authAdapter;
    }

    @GetMapping
    public List<EventResponse> getAllEvents(
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {
        String email = authAdapter.getEmailOrNull(oauthUser, principal);
        if (email != null) {
            return eventService.getAllEventsForUser(email);
        }
        return eventService.getAllEvents();
    }

    @GetMapping("/category")
    public List<EventResponse> getByCategory(@RequestParam String category) {
        return eventService.getEventsByCategory(category);
    }

    @GetMapping("/my-events")
    public List<EventResponse> getMyEvents(
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {
        String email = authAdapter.getEmail(oauthUser, principal);
        return eventService.getEventsByOrganizer(email);
    }

    @PostMapping
    public EventResponse createEvent(
            @RequestBody EventRequest request,
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {
        String email = authAdapter.getEmail(oauthUser, principal);
        return eventService.createEvent(request, email);
    }

    @PostMapping(consumes = "multipart/form-data")
    public EventResponse createEvent(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String location,
            @RequestParam String category,
            @RequestParam Integer maxParticipants,
            @RequestParam(required = false) MultipartFile image,
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) throws IOException {

        EventRequest request = new EventRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setStartDate(LocalDateTime.parse(startDate));
        request.setEndDate(LocalDateTime.parse(endDate));
        request.setLocation(location);
        request.setCategory(category);
        request.setMaxParticipants(maxParticipants);

        String email = authAdapter.getEmail(oauthUser, principal);
        return eventService.createEvent(request, email, image);
    }

    // Update event details (organizer only)
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String location,
            @RequestParam String category,
            @RequestParam Integer maxParticipants,
            @RequestParam(required = false) MultipartFile image,
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {
        try {
            String email = authAdapter.getEmail(oauthUser, principal);

            EventRequest request = new EventRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setStartDate(LocalDateTime.parse(startDate));
            request.setEndDate(LocalDateTime.parse(endDate));
            request.setLocation(location);
            request.setCategory(category);
            request.setMaxParticipants(maxParticipants);

            EventResponse response = eventService.updateEvent(id, request, email, image);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "File upload failed"));
        }
    }

    @PatchMapping("/{id}/status")
    public EventResponse updateStatus(
            @PathVariable Long id,
            @RequestParam EventStatus status) {
        return eventService.updateEventStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }

    // ── Registration ──

    @PostMapping("/{id}/register")
    public ResponseEntity<?> registerForEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {
        try {
            String email = authAdapter.getEmail(oauthUser, principal);;
            EventResponse response = eventService.registerForEvent(id, email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/registered")
    public List<EventResponse> getRegisteredEvents(
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {
        String email = authAdapter.getEmail(oauthUser, principal);
        return eventService.getRegisteredEvents(email);
    }

}