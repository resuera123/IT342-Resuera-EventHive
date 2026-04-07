package edu.cit.resuera.eventhive.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.cit.resuera.eventhive.dto.EventRequest;
import edu.cit.resuera.eventhive.dto.EventResponse;
import edu.cit.resuera.eventhive.entity.Event;
import edu.cit.resuera.eventhive.entity.EventRegistration;
import edu.cit.resuera.eventhive.entity.EventStatus;
import edu.cit.resuera.eventhive.entity.User;
import edu.cit.resuera.eventhive.event.EventStatusChangedEvent;
import edu.cit.resuera.eventhive.repository.EventRegistrationRepository;
import edu.cit.resuera.eventhive.repository.EventRepository;
import edu.cit.resuera.eventhive.repository.UserRepository;
import edu.cit.resuera.eventhive.validator.RegistrationValidator;

/**
 * Refactored EventService applying:
 *  - Facade Pattern: implements IEventService interface, hides subsystem complexity
 *  - Builder Pattern: uses EventResponse.builder() instead of 15-param constructor
 *  - Observer Pattern: publishes EventStatusChangedEvent instead of calling NotificationService directly
 *  - Strategy Pattern: delegates registration validation to injected RegistrationValidator list
 */
@Service
public class EventService implements IEventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventRegistrationRepository registrationRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;          // Observer
    private final List<RegistrationValidator> registrationValidators; // Strategy

    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        EventRegistrationRepository registrationRepository,
                        NotificationService notificationService,
                        ApplicationEventPublisher eventPublisher,
                        List<RegistrationValidator> registrationValidators) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.registrationRepository = registrationRepository;
        this.notificationService = notificationService;
        this.eventPublisher = eventPublisher;
        this.registrationValidators = registrationValidators;
    }

    @Override
    public EventResponse createEvent(EventRequest request, String organizerEmail) {
        User organizer = findUserByEmail(organizerEmail);

        Event event = new Event();
        applyRequest(event, request);
        event.setStatus(EventStatus.UPCOMING);
        event.setOrganizer(organizer);
        event.setCreatedAt(LocalDateTime.now());

        return toResponse(eventRepository.save(event), null);
    }

    @Override
    public EventResponse createEvent(EventRequest request, String organizerEmail, MultipartFile image) throws IOException {
        User organizer = findUserByEmail(organizerEmail);

        Event event = new Event();
        applyRequest(event, request);
        event.setImageUrl(saveImage(image));
        event.setStatus(EventStatus.UPCOMING);
        event.setOrganizer(organizer);
        event.setCreatedAt(LocalDateTime.now());

        return toResponse(eventRepository.save(event), null);
    }

    @Override
    public EventResponse updateEvent(Long eventId, EventRequest request, String organizerEmail, MultipartFile image) throws IOException {
        Event event = findEventById(eventId);

        if (!event.getOrganizer().getEmail().equals(organizerEmail)) {
            throw new RuntimeException("Only the organizer can edit this event");
        }

        applyRequest(event, request);
        if (image != null && !image.isEmpty()) {
            event.setImageUrl(saveImage(image));
        }

        return toResponse(eventRepository.save(event), null);
    }

    // ── Observer Pattern: publish event instead of calling notification service directly ──
    @Override
    public EventResponse updateEventStatus(Long eventId, EventStatus status) {
        Event event = findEventById(eventId);
        EventStatus oldStatus = event.getStatus();
        event.setStatus(status);
        Event saved = eventRepository.save(event);

        // Publish event — listeners handle notifications (Observer Pattern)
        eventPublisher.publishEvent(new EventStatusChangedEvent(this, saved, oldStatus, status));

        return toResponse(saved, null);
    }

    @Override
    public void deleteEvent(Long eventId) {
        eventRepository.deleteById(eventId);
    }

    @Override
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(e -> toResponse(e, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventResponse> getAllEventsForUser(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        return eventRepository.findAll().stream()
                .map(e -> toResponse(e, user))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventResponse> getEventsByOrganizer(String organizerEmail) {
        User organizer = findUserByEmail(organizerEmail);
        return eventRepository.findByOrganizer(organizer).stream()
                .map(e -> toResponse(e, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventResponse> getEventsByCategory(String category) {
        return eventRepository.findByCategory(category).stream()
                .map(e -> toResponse(e, null))
                .collect(Collectors.toList());
    }

    // ── Strategy Pattern: delegates validation to injected validator list ──
    @Override
    public EventResponse registerForEvent(Long eventId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Event event = findEventById(eventId);

        // Strategy Pattern — each validator checks one rule
        registrationValidators.forEach(v -> v.validate(user, event));

        EventRegistration registration = new EventRegistration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setRegisteredAt(LocalDateTime.now());
        registrationRepository.save(registration);

        notificationService.notifyRegistration(user, event);

        return toResponse(event, user);
    }

    @Override
    public List<EventResponse> getRegisteredEvents(String userEmail) {
        User user = findUserByEmail(userEmail);
        return registrationRepository.findByUser(user).stream()
                .map(reg -> toResponse(reg.getEvent(), user))
                .collect(Collectors.toList());
    }

    // ── Builder Pattern: constructs EventResponse with named setters ──
    private EventResponse toResponse(Event event, User currentUser) {
        int count = registrationRepository.countByEvent(event);
        Boolean isRegistered = currentUser != null
                ? registrationRepository.existsByUserAndEvent(currentUser, event)
                : null;

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .category(event.getCategory())
                .imageUrl(event.getImageUrl())
                .maxParticipants(event.getMaxParticipants())
                .status(event.getStatus())
                .organizerId(event.getOrganizer().getId())
                .organizerName(event.getOrganizer().getFirstname() + " " + event.getOrganizer().getLastname())
                .createdAt(event.getCreatedAt())
                .participantCount(count)
                .isRegistered(isRegistered)
                .build();
    }

    // ── Private helpers ──

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    private void applyRequest(Event event, EventRequest request) {
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocation(request.getLocation());
        event.setCategory(request.getCategory());
        event.setMaxParticipants(request.getMaxParticipants());
    }

    private String saveImage(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) return null;
        String uploadDir = "uploads/events/";
        java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
        if (!java.nio.file.Files.exists(uploadPath)) {
            java.nio.file.Files.createDirectories(uploadPath);
        }
        String filename = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        java.nio.file.Files.copy(image.getInputStream(), uploadPath.resolve(filename));
        return "/" + uploadDir + filename;
    }
}