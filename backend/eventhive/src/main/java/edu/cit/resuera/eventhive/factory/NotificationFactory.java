package edu.cit.resuera.eventhive.factory;

import java.time.LocalDateTime;

import edu.cit.resuera.eventhive.entity.Event;
import edu.cit.resuera.eventhive.entity.Notification;
import edu.cit.resuera.eventhive.entity.User;

public class NotificationFactory {

    private NotificationFactory() {} // Prevent instantiation

    public static Notification registration(User participant, Event event) {
        return create(participant, "REGISTRATION", "Registration Confirmed",
                "You have successfully registered for \"" + event.getTitle() + "\".",
                event.getId());
    }

    public static Notification newParticipant(User organizer, User participant, Event event) {
        return create(organizer, "NEW_PARTICIPANT", "New Participant",
                participant.getFirstname() + " " + participant.getLastname()
                        + " has registered for your event \"" + event.getTitle() + "\".",
                event.getId());
    }

    public static Notification eventCancelled(User participant, Event event) {
        return create(participant, "EVENT_CANCELLED", "Event Cancelled",
                "The event \"" + event.getTitle() + "\" has been cancelled by the organizer.",
                event.getId());
    }

    public static Notification eventResumed(User participant, Event event) {
        return create(participant, "EVENT_RESUMED", "Event Resumed",
                "The event \"" + event.getTitle() + "\" has been resumed and is now upcoming again.",
                event.getId());
    }

    private static Notification create(User user, String type, String title, String message, Long eventId) {
        Notification notif = new Notification();
        notif.setUser(user);
        notif.setType(type);
        notif.setTitle(title);
        notif.setMessage(message);
        notif.setRead(false);
        notif.setEventId(eventId);
        notif.setCreatedAt(LocalDateTime.now());
        return notif;
    }
}