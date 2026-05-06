package edu.cit.resuera.eventhive.event;

import edu.cit.resuera.eventhive.event.dto.EventRequest;
import edu.cit.resuera.eventhive.event.dto.EventResponse;
import edu.cit.resuera.eventhive.notification.NotificationService;
import edu.cit.resuera.eventhive.shared.storage.SupabaseStorageService;
import edu.cit.resuera.eventhive.user.User;
import edu.cit.resuera.eventhive.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventService — exercises business rules in isolation
 * with all repository dependencies mocked. Fast, hermetic.
 */
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private EventRegistrationRepository registrationRepository;
    @Mock private NotificationService notificationService;
    @Mock private SupabaseStorageService storageService;

    @InjectMocks private EventService eventService;

    private User organizer;
    private User participant;
    private Event sampleEvent;

    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setId(1L);
        organizer.setEmail("organizer@example.com");
        organizer.setFirstname("Olivia");
        organizer.setLastname("Organizer");
        organizer.setRole("organizer");
        organizer.setCreatedAt(LocalDateTime.now());

        participant = new User();
        participant.setId(2L);
        participant.setEmail("participant@example.com");
        participant.setFirstname("Pat");
        participant.setLastname("Participant");
        participant.setRole("participant");
        participant.setCreatedAt(LocalDateTime.now());

        sampleEvent = new Event();
        sampleEvent.setId(100L);
        sampleEvent.setTitle("Test Event");
        sampleEvent.setDescription("Sample description");
        sampleEvent.setStartDate(LocalDateTime.now().plusDays(1));
        sampleEvent.setEndDate(LocalDateTime.now().plusDays(2));
        sampleEvent.setLocation("Online");
        sampleEvent.setCategory("Tech");
        sampleEvent.setMaxParticipants(100);
        sampleEvent.setStatus(EventStatus.UPCOMING);
        sampleEvent.setOrganizer(organizer);
        sampleEvent.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("createEvent (JSON) saves event with UPCOMING status and correct organizer")
    void createEvent_jsonOnly_setsStatusAndOrganizer() {
        EventRequest request = new EventRequest();
        request.setTitle("New Event");
        request.setDescription("Description");
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(2));
        request.setLocation("Cebu");
        request.setCategory("Music");
        request.setMaxParticipants(50);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> {
            Event e = inv.getArgument(0);
            e.setId(101L);
            return e;
        });
        when(registrationRepository.countByEvent(any(Event.class))).thenReturn(0);

        EventResponse response = eventService.createEvent(request, "organizer@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(EventStatus.UPCOMING);
        assertThat(response.getOrganizerId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("New Event");
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("createEvent fails when organizer email is unknown")
    void createEvent_unknownOrganizer_throws() {
        EventRequest request = new EventRequest();
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.createEvent(request, "ghost@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Organizer not found");

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("createEvent (multipart) with image uploads to Supabase and stores URL")
    void createEvent_multipart_uploadsToSupabase() throws IOException {
        EventRequest request = new EventRequest();
        request.setTitle("Image Event");
        request.setDescription("With image");
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(2));
        request.setLocation("Online");
        request.setCategory("Arts");
        request.setMaxParticipants(20);

        org.springframework.mock.web.MockMultipartFile image = new org.springframework.mock.web.MockMultipartFile(
                "image", "pic.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        String supabaseUrl = "https://xxx.supabase.co/storage/v1/object/public/event-images/pic.jpg";

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(storageService.uploadEventImage(image)).thenReturn(supabaseUrl);
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> {
            Event e = inv.getArgument(0);
            e.setId(102L);
            return e;
        });
        when(registrationRepository.countByEvent(any(Event.class))).thenReturn(0);

        EventResponse response = eventService.createEvent(request, "organizer@example.com", image);

        assertThat(response.getImageUrl()).isEqualTo(supabaseUrl);
        verify(storageService).uploadEventImage(image);
    }

    @Test
    @DisplayName("updateEventStatus to CANCELLED triggers cancellation notifications")
    void updateEventStatus_toCancelled_firesNotifications() {
        sampleEvent.setStatus(EventStatus.UPCOMING);
        when(eventRepository.findById(100L)).thenReturn(Optional.of(sampleEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(sampleEvent);
        when(registrationRepository.countByEvent(any(Event.class))).thenReturn(5);

        EventResponse response = eventService.updateEventStatus(100L, EventStatus.CANCELLED);

        assertThat(response.getStatus()).isEqualTo(EventStatus.CANCELLED);
        verify(notificationService).notifyEventCancelled(sampleEvent);
        verify(notificationService, never()).notifyEventResumed(any());
    }

    @Test
    @DisplayName("updateEventStatus from CANCELLED back to UPCOMING triggers resumed notifications")
    void updateEventStatus_resumeFromCancelled_firesResumedNotifications() {
        sampleEvent.setStatus(EventStatus.CANCELLED);
        when(eventRepository.findById(100L)).thenReturn(Optional.of(sampleEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(sampleEvent);
        when(registrationRepository.countByEvent(any(Event.class))).thenReturn(3);

        EventResponse response = eventService.updateEventStatus(100L, EventStatus.UPCOMING);

        assertThat(response.getStatus()).isEqualTo(EventStatus.UPCOMING);
        verify(notificationService).notifyEventResumed(sampleEvent);
        verify(notificationService, never()).notifyEventCancelled(any());
    }

    @Test
    @DisplayName("updateEventStatus to same status fires no notifications")
    void updateEventStatus_sameStatus_noNotifications() {
        sampleEvent.setStatus(EventStatus.UPCOMING);
        when(eventRepository.findById(100L)).thenReturn(Optional.of(sampleEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(sampleEvent);
        when(registrationRepository.countByEvent(any(Event.class))).thenReturn(0);

        eventService.updateEventStatus(100L, EventStatus.UPCOMING);

        verify(notificationService, never()).notifyEventCancelled(any());
        verify(notificationService, never()).notifyEventResumed(any());
    }

    @Test
    @DisplayName("registerForEvent succeeds and fires notification")
    void registerForEvent_validParticipant_succeeds() {
        when(userRepository.findByEmail("participant@example.com")).thenReturn(Optional.of(participant));
        when(eventRepository.findById(100L)).thenReturn(Optional.of(sampleEvent));
        when(registrationRepository.existsByUserAndEvent(participant, sampleEvent)).thenReturn(false);
        when(registrationRepository.countByEvent(sampleEvent)).thenReturn(10);

        EventResponse response = eventService.registerForEvent(100L, "participant@example.com");

        assertThat(response).isNotNull();
        verify(registrationRepository).save(any(EventRegistration.class));
        verify(notificationService).notifyRegistration(participant, sampleEvent);
    }

    @Test
    @DisplayName("registerForEvent rejects organizer registering for own event")
    void registerForEvent_ownEvent_throws() {
        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.findById(100L)).thenReturn(Optional.of(sampleEvent));

        assertThatThrownBy(() -> eventService.registerForEvent(100L, "organizer@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Organizers cannot register");

        verify(registrationRepository, never()).save(any());
        verify(notificationService, never()).notifyRegistration(any(), any());
    }

    @Test
    @DisplayName("registerForEvent rejects duplicate registration")
    void registerForEvent_alreadyRegistered_throws() {
        when(userRepository.findByEmail("participant@example.com")).thenReturn(Optional.of(participant));
        when(eventRepository.findById(100L)).thenReturn(Optional.of(sampleEvent));
        when(registrationRepository.existsByUserAndEvent(participant, sampleEvent)).thenReturn(true);

        assertThatThrownBy(() -> eventService.registerForEvent(100L, "participant@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Already registered");

        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerForEvent rejects when event is full")
    void registerForEvent_eventFull_throws() {
        sampleEvent.setMaxParticipants(5);
        when(userRepository.findByEmail("participant@example.com")).thenReturn(Optional.of(participant));
        when(eventRepository.findById(100L)).thenReturn(Optional.of(sampleEvent));
        when(registrationRepository.existsByUserAndEvent(participant, sampleEvent)).thenReturn(false);
        when(registrationRepository.countByEvent(sampleEvent)).thenReturn(5);

        assertThatThrownBy(() -> eventService.registerForEvent(100L, "participant@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Event is full");
    }

    @Test
    @DisplayName("updateEvent allows organizer to edit own event")
    void updateEvent_byOrganizer_succeeds() throws IOException {
        EventRequest request = new EventRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated description");
        request.setStartDate(LocalDateTime.now().plusDays(2));
        request.setEndDate(LocalDateTime.now().plusDays(3));
        request.setLocation("Updated Location");
        request.setCategory("Sports");
        request.setMaxParticipants(75);

        when(eventRepository.findById(100L)).thenReturn(Optional.of(sampleEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(sampleEvent);
        when(registrationRepository.countByEvent(any(Event.class))).thenReturn(0);

        EventResponse response = eventService.updateEvent(100L, request, "organizer@example.com", null);

        assertThat(response.getTitle()).isEqualTo("Updated Title");
        verify(eventRepository).save(sampleEvent);
    }

    @Test
    @DisplayName("updateEvent rejects non-organizer attempting to edit")
    void updateEvent_byNonOrganizer_throws() {
        EventRequest request = new EventRequest();
        request.setTitle("Hijack Attempt");

        when(eventRepository.findById(100L)).thenReturn(Optional.of(sampleEvent));

        assertThatThrownBy(() ->
                eventService.updateEvent(100L, request, "stranger@example.com", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only the organizer can edit");

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteEvent removes event and attempts image cleanup")
    void deleteEvent_callsStorageDeleteAndRepositoryDelete() {
        sampleEvent.setImageUrl("https://xxx.supabase.co/storage/v1/object/public/event-images/x.jpg");
        when(eventRepository.findById(100L)).thenReturn(Optional.of(sampleEvent));

        eventService.deleteEvent(100L);

        verify(storageService).deleteByPublicUrl(sampleEvent.getImageUrl());
        verify(eventRepository).deleteById(100L);
    }

    @Test
    @DisplayName("getEventsByOrganizer returns events filtered by organizer")
    void getEventsByOrganizer_returnsFilteredList() {
        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.findByOrganizer(organizer)).thenReturn(List.of(sampleEvent));
        when(registrationRepository.countByEvent(sampleEvent)).thenReturn(2);

        List<EventResponse> result = eventService.getEventsByOrganizer("organizer@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrganizerId()).isEqualTo(1L);
    }
}