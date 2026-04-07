package edu.cit.resuera.eventhive.event;

import org.springframework.context.ApplicationEvent;

import edu.cit.resuera.eventhive.entity.Event;
import edu.cit.resuera.eventhive.entity.EventStatus;

public class EventStatusChangedEvent extends ApplicationEvent {

    private final Event event;
    private final EventStatus oldStatus;
    private final EventStatus newStatus;

    public EventStatusChangedEvent(Object source, Event event, EventStatus oldStatus, EventStatus newStatus) {
        super(source);
        this.event = event;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public Event getEvent() { return event; }
    public EventStatus getOldStatus() { return oldStatus; }
    public EventStatus getNewStatus() { return newStatus; }
}