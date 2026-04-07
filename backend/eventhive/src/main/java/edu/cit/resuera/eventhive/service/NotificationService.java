package edu.cit.resuera.eventhive.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import edu.cit.resuera.eventhive.entity.Event;
import edu.cit.resuera.eventhive.entity.Notification;
import edu.cit.resuera.eventhive.entity.User;
import edu.cit.resuera.eventhive.repository.EventRegistrationRepository;
import edu.cit.resuera.eventhive.repository.NotificationRepository;
import edu.cit.resuera.eventhive.repository.UserRepository;
import edu.cit.resuera.eventhive.factory.NotificationFactory;

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
        User user = findUser(email);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public int getUnreadCount(String email) {
        User user = findUser(email);
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
        User user = findUser(email);
        List<Notification> unread = notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // ── Create notifications ──

    public void notifyRegistration(User participant, Event event) {
        // Factory creates the notification objects
        notificationRepository.save(NotificationFactory.registration(participant, event));
        notificationRepository.save(NotificationFactory.newParticipant(event.getOrganizer(), participant, event));
    }

    public void notifyEventCancelled(Event event) {
        registrationRepository.findByEvent(event).forEach(reg ->
                notificationRepository.save(NotificationFactory.eventCancelled(reg.getUser(), event))
        );
    }

    public void notifyEventResumed(Event event) {
        registrationRepository.findByEvent(event).forEach(reg ->
                notificationRepository.save(NotificationFactory.eventResumed(reg.getUser(), event))
        );
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}