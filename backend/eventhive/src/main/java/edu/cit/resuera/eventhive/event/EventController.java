package edu.cit.resuera.eventhive.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

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

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // Get all events
    @GetMapping
    public List<EventResponse> getAllEvents() {
        return eventService.getAllEvents();
    }

    // Get events by category
    @GetMapping("/category")
    public List<EventResponse> getByCategory(@RequestParam String category) {
        return eventService.getEventsByCategory(category);
    }

    // Get events created by logged-in organizer
    @GetMapping("/my-events")
    public List<EventResponse> getMyEvents(
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {
        String email = resolveEmail(oauthUser, principal);
        return eventService.getEventsByOrganizer(email);
    }

    // ══════════════════════════════════════════════
    // FIX: Disambiguate the two POST endpoints
    // The JSON one is for the web frontend
    // The multipart one is for mobile + web file uploads
    // ══════════════════════════════════════════════

    // Create event — JSON body (web frontend without image)
    @PostMapping(consumes = "application/json")
    public EventResponse createEventJson(
            @RequestBody EventRequest request,
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {
        String email = resolveEmail(oauthUser, principal);
        return eventService.createEvent(request, email);
    }

    // Create event — multipart (mobile + web with image)
    @PostMapping(consumes = "multipart/form-data")
    public EventResponse createEventMultipart(
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

        String email = resolveEmail(oauthUser, principal);
        return eventService.createEvent(request, email, image);
    }

    // Update event — multipart (mobile + web)
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public EventResponse updateEvent(
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
            Principal principal) throws IOException {

        String email = resolveEmail(oauthUser, principal);

        EventRequest request = new EventRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setStartDate(LocalDateTime.parse(startDate));
        request.setEndDate(LocalDateTime.parse(endDate));
        request.setLocation(location);
        request.setCategory(category);
        request.setMaxParticipants(maxParticipants);

        return eventService.updateEvent(id, request, email, image);
    }

    // Update event status
    @PatchMapping("/{id}/status")
    public EventResponse updateStatus(
            @PathVariable Long id,
            @RequestParam EventStatus status) {
        return eventService.updateEventStatus(id, status);
    }

    // Delete event
    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }

    // Register for event
    @PostMapping("/{id}/register")
    public EventResponse registerForEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {
        String email = resolveEmail(oauthUser, principal);
        return eventService.registerForEvent(id, email);
    }

    // Get registered events
    @GetMapping("/registered")
    public List<EventResponse> getRegisteredEvents(
            @AuthenticationPrincipal OAuth2User oauthUser,
            Principal principal) {
        String email = resolveEmail(oauthUser, principal);
        return eventService.getRegisteredEvents(email);
    }

    private String resolveEmail(OAuth2User oauthUser, Principal principal) {
        if (oauthUser != null) return oauthUser.getAttribute("email");
        if (principal != null) return principal.getName();
        throw new RuntimeException("Not authenticated");
    }
}