package edu.cit.resuera.eventhive.notification;

import edu.cit.resuera.eventhive.event.Event;
import edu.cit.resuera.eventhive.event.EventRegistration;
import edu.cit.resuera.eventhive.event.EventRegistrationRepository;
import edu.cit.resuera.eventhive.event.EventStatus;
import edu.cit.resuera.eventhive.user.User;
import edu.cit.resuera.eventhive.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private EventRegistrationRepository registrationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private NotificationService notificationService;

    private User organizer;
    private User participant;
    private Event event;

    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setId(1L);
        organizer.setEmail("organizer@example.com");
        organizer.setFirstname("Olivia");
        organizer.setLastname("Organizer");

        participant = new User();
        participant.setId(2L);
        participant.setEmail("participant@example.com");
        participant.setFirstname("Pat");
        participant.setLastname("Participant");

        event = new Event();
        event.setId(100L);
        event.setTitle("Music Fest");
        event.setOrganizer(organizer);
        event.setStatus(EventStatus.UPCOMING);
        event.setStartDate(LocalDateTime.now().plusDays(1));
        event.setEndDate(LocalDateTime.now().plusDays(2));
    }

    @Test
    @DisplayName("notifyRegistration creates one notification for participant and one for organizer")
    void notifyRegistration_createsTwoNotifications() {
        notificationService.notifyRegistration(participant, event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        List<Notification> saved = captor.getAllValues();
        assertThat(saved).hasSize(2);

        Notification participantNotif = saved.stream()
                .filter(n -> n.getUser().getId().equals(participant.getId()))
                .findFirst().orElseThrow();
        assertThat(participantNotif.getType()).isEqualTo("REGISTRATION");
        assertThat(participantNotif.getTitle()).isEqualTo("Registration Confirmed");
        assertThat(participantNotif.isRead()).isFalse();

        Notification organizerNotif = saved.stream()
                .filter(n -> n.getUser().getId().equals(organizer.getId()))
                .findFirst().orElseThrow();
        assertThat(organizerNotif.getType()).isEqualTo("NEW_PARTICIPANT");
        assertThat(organizerNotif.getMessage()).contains("Pat Participant");
    }

    @Test
    @DisplayName("notifyEventCancelled creates one notification per registered participant")
    void notifyEventCancelled_iteratesAllParticipants() {
        EventRegistration reg1 = new EventRegistration();
        reg1.setUser(participant);
        reg1.setEvent(event);

        User other = new User();
        other.setId(3L);
        other.setEmail("other@example.com");
        EventRegistration reg2 = new EventRegistration();
        reg2.setUser(other);
        reg2.setEvent(event);

        when(registrationRepository.findByEvent(event)).thenReturn(List.of(reg1, reg2));

        notificationService.notifyEventCancelled(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        captor.getAllValues().forEach(n -> {
            assertThat(n.getType()).isEqualTo("EVENT_CANCELLED");
            assertThat(n.getTitle()).isEqualTo("Event Cancelled");
            assertThat(n.getMessage()).contains("Music Fest");
        });
    }

    @Test
    @DisplayName("notifyEventResumed broadcasts EVENT_RESUMED to all participants")
    void notifyEventResumed_setsCorrectType() {
        EventRegistration reg = new EventRegistration();
        reg.setUser(participant);
        reg.setEvent(event);
        when(registrationRepository.findByEvent(event)).thenReturn(List.of(reg));

        notificationService.notifyEventResumed(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification n = captor.getValue();
        assertThat(n.getType()).isEqualTo("EVENT_RESUMED");
        assertThat(n.getTitle()).isEqualTo("Event Resumed");
    }

    @Test
    @DisplayName("notifyEventCancelled with no participants saves no notifications")
    void notifyEventCancelled_noParticipants_noopSafely() {
        when(registrationRepository.findByEvent(event)).thenReturn(Collections.emptyList());

        notificationService.notifyEventCancelled(event);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("markAsRead flips read flag when caller owns the notification")
    void markAsRead_byOwner_succeeds() {
        Notification notif = new Notification();
        notif.setId(50L);
        notif.setUser(participant);
        notif.setRead(false);

        when(notificationRepository.findById(50L)).thenReturn(Optional.of(notif));

        notificationService.markAsRead(50L, "participant@example.com");

        assertThat(notif.isRead()).isTrue();
        verify(notificationRepository).save(notif);
    }

    @Test
    @DisplayName("markAsRead rejects user trying to read another user's notification")
    void markAsRead_byNonOwner_throws() {
        Notification notif = new Notification();
        notif.setId(50L);
        notif.setUser(participant);
        notif.setRead(false);

        when(notificationRepository.findById(50L)).thenReturn(Optional.of(notif));

        assertThatThrownBy(() ->
                notificationService.markAsRead(50L, "stranger@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Not authorized");

        verify(notificationRepository, never()).save(any());
    }
}