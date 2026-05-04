package edu.cit.resuera.eventhive.notification;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import edu.cit.resuera.eventhive.event.Event;
import edu.cit.resuera.eventhive.notification.Notification;
import edu.cit.resuera.eventhive.user.User;
import edu.cit.resuera.eventhive.event.EventRegistrationRepository;
import edu.cit.resuera.eventhive.notification.NotificationRepository;
import edu.cit.resuera.eventhive.user.UserRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EventRegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               EventRegistrationRepository registrationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
    }

    // ── Fetch ──

    public List<Notification> getUserNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public int getUnreadCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.countByUserAndReadFalse(user);
    }

    public void markAsRead(Long notificationId, String email) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!notif.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Not authorized");
        }
        notif.setRead(true);
        notificationRepository.save(notif);
    }

    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Notification> unread = notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // ── Create notifications ──

    /**
     * When a participant registers for an event:
     * - Notify the participant (confirmation)
     * - Notify the organizer (new participant)
     */
    public void notifyRegistration(User participant, Event event) {
        // To participant
        create(participant, "REGISTRATION", "Registration Confirmed",
                "You have successfully registered for \"" + event.getTitle() + "\".",
                event.getId());

        // To organizer
        create(event.getOrganizer(), "NEW_PARTICIPANT",
                "New Participant",
                participant.getFirstname() + " " + participant.getLastname()
                        + " has registered for your event \"" + event.getTitle() + "\".",
                event.getId());
    }

    /**
     * When an organizer cancels an event:
     * - Notify all registered participants
     */
    public void notifyEventCancelled(Event event) {
        String msg = "The event \"" + event.getTitle() + "\" has been cancelled by the organizer.";
        notifyAllParticipants(event, "EVENT_CANCELLED", "Event Cancelled", msg);
    }

    /**
     * When an organizer resumes a cancelled event:
     * - Notify all registered participants
     */
    public void notifyEventResumed(Event event) {
        String msg = "The event \"" + event.getTitle() + "\" has been resumed and is now upcoming again.";
        notifyAllParticipants(event, "EVENT_RESUMED", "Event Resumed", msg);
    }

    // ── Helpers ──

    private void notifyAllParticipants(Event event, String type, String title, String message) {
        registrationRepository.findByEvent(event).forEach(reg ->
                create(reg.getUser(), type, title, message, event.getId())
        );
    }

    private void create(User user, String type, String title, String message, Long eventId) {
        Notification notif = new Notification();
        notif.setUser(user);
        notif.setType(type);
        notif.setTitle(title);
        notif.setMessage(message);
        notif.setRead(false);
        notif.setEventId(eventId);
        notif.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notif);
    }
}