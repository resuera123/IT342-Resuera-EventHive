package edu.cit.resuera.eventhive.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import edu.cit.resuera.eventhive.entity.EventStatus;
import edu.cit.resuera.eventhive.service.NotificationService;

@Component
public class EventNotificationListener {

    private final NotificationService notificationService;

    public EventNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onStatusChanged(EventStatusChangedEvent event) {
        EventStatus newStatus = event.getNewStatus();
        EventStatus oldStatus = event.getOldStatus();

        if (newStatus == EventStatus.CANCELLED && oldStatus != EventStatus.CANCELLED) {
            notificationService.notifyEventCancelled(event.getEvent());
        } else if (newStatus == EventStatus.UPCOMING && oldStatus == EventStatus.CANCELLED) {
            notificationService.notifyEventResumed(event.getEvent());
        }
    }
}