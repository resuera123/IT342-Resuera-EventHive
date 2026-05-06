package edu.cit.resuera.eventhive.event;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import edu.cit.resuera.eventhive.notification.NotificationService;
import edu.cit.resuera.eventhive.event.dto.EventRequest;
import edu.cit.resuera.eventhive.event.dto.EventResponse;
import edu.cit.resuera.eventhive.event.Event;
import edu.cit.resuera.eventhive.event.EventRegistration;
import edu.cit.resuera.eventhive.event.EventStatus;
import edu.cit.resuera.eventhive.shared.storage.SupabaseStorageService;
import edu.cit.resuera.eventhive.user.User;
import edu.cit.resuera.eventhive.event.EventRegistrationRepository;
import edu.cit.resuera.eventhive.event.EventRepository;
import edu.cit.resuera.eventhive.user.UserRepository;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventRegistrationRepository registrationRepository;
    private final NotificationService notificationService;
    private final SupabaseStorageService storageService;

    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        EventRegistrationRepository registrationRepository,
                        NotificationService notificationService,
                        SupabaseStorageService storageService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.registrationRepository = registrationRepository;
        this.notificationService = notificationService;
        this.storageService = storageService;
    }

    public EventResponse createEvent(EventRequest request, String organizerEmail) {
        User organizer = userRepository.findByEmail(organizerEmail)
            .orElseThrow(() -> new RuntimeException("Organizer not found"));

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocation(request.getLocation());
        event.setCategory(request.getCategory());
        event.setMaxParticipants(request.getMaxParticipants());
        event.setStatus(EventStatus.UPCOMING);
        event.setOrganizer(organizer);
        event.setCreatedAt(LocalDateTime.now());

        Event saved = eventRepository.save(event);
        return toResponse(saved, null);
    }

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
            .stream()
            .map(e -> toResponse(e, null))
            .collect(Collectors.toList());
    }

    public List<EventResponse> getAllEventsForUser(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        return eventRepository.findAll()
            .stream()
            .map(e -> toResponse(e, user))
            .collect(Collectors.toList());
    }

    public List<EventResponse> getEventsByOrganizer(String organizerEmail) {
        User organizer = userRepository.findByEmail(organizerEmail)
            .orElseThrow(() -> new RuntimeException("Organizer not found"));
        return eventRepository.findByOrganizer(organizer)
            .stream()
            .map(e -> toResponse(e, null))
            .collect(Collectors.toList());
    }

    public List<EventResponse> getEventsByCategory(String category) {
        return eventRepository.findByCategory(category)
            .stream()
            .map(e -> toResponse(e, null))
            .collect(Collectors.toList());
    }

    public EventResponse updateEventStatus(Long eventId, EventStatus status) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));

        EventStatus oldStatus = event.getStatus();
        event.setStatus(status);
        Event saved = eventRepository.save(event);

        // Send notifications
        if (status == EventStatus.CANCELLED && oldStatus != EventStatus.CANCELLED) {
            notificationService.notifyEventCancelled(saved);
        } else if (status == EventStatus.UPCOMING && oldStatus == EventStatus.CANCELLED) {
            notificationService.notifyEventResumed(saved);
        }

        return toResponse(saved, null);
    }

    public EventResponse updateEvent(Long eventId, EventRequest request, String organizerEmail, MultipartFile image) throws IOException {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getOrganizer().getEmail().equals(organizerEmail)) {
            throw new RuntimeException("Only the organizer can edit this event");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocation(request.getLocation());
        event.setCategory(request.getCategory());
        event.setMaxParticipants(request.getMaxParticipants());

        if (image != null && !image.isEmpty()) {
            // Delete the previous image from Supabase, then upload the new one.
            // Best-effort delete — don't block the update if cleanup fails.
            String previousUrl = event.getImageUrl();
            String newUrl = storageService.uploadEventImage(image);
            event.setImageUrl(newUrl);
            storageService.deleteByPublicUrl(previousUrl);
        }

        return toResponse(eventRepository.save(event), null);
    }

    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));

        // Best-effort cleanup of the image in Supabase before deleting the row
        storageService.deleteByPublicUrl(event.getImageUrl());

        eventRepository.deleteById(eventId);
    }

    public EventResponse createEvent(EventRequest request, String organizerEmail, MultipartFile image) throws IOException {
        User organizer = userRepository.findByEmail(organizerEmail)
            .orElseThrow(() -> new RuntimeException("Organizer not found"));

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = storageService.uploadEventImage(image);
        }

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocation(request.getLocation());
        event.setCategory(request.getCategory());
        event.setImageUrl(imageUrl);
        event.setMaxParticipants(request.getMaxParticipants());
        event.setStatus(EventStatus.UPCOMING);
        event.setOrganizer(organizer);
        event.setCreatedAt(LocalDateTime.now());

        return toResponse(eventRepository.save(event), null);
    }

    // ── Registration ──

    public EventResponse registerForEvent(Long eventId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getOrganizer().getId().equals(user.getId())) {
            throw new RuntimeException("Organizers cannot register for their own events");
        }

        if (registrationRepository.existsByUserAndEvent(user, event)) {
            throw new RuntimeException("Already registered for this event");
        }

        int current = registrationRepository.countByEvent(event);
        if (event.getMaxParticipants() != null && current >= event.getMaxParticipants()) {
            throw new RuntimeException("Event is full");
        }

        EventRegistration registration = new EventRegistration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setRegisteredAt(LocalDateTime.now());
        registrationRepository.save(registration);

        // Send notifications
        notificationService.notifyRegistration(user, event);

        return toResponse(event, user);
    }

    public List<EventResponse> getRegisteredEvents(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return registrationRepository.findByUser(user)
            .stream()
            .map(reg -> toResponse(reg.getEvent(), user))
            .collect(Collectors.toList());
    }

    // ── Mapping ──

    private EventResponse toResponse(Event event, User currentUser) {
        int count = registrationRepository.countByEvent(event);
        Boolean isRegistered = null;
        if (currentUser != null) {
            isRegistered = registrationRepository.existsByUserAndEvent(currentUser, event);
        }

        return new EventResponse(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getStartDate(),
            event.getEndDate(),
            event.getLocation(),
            event.getCategory(),
            event.getImageUrl(),
            event.getMaxParticipants(),
            event.getStatus(),
            event.getOrganizer().getId(),
            event.getOrganizer().getFirstname() + " " + event.getOrganizer().getLastname(),
            event.getCreatedAt(),
            count,
            isRegistered
        );
    }
}