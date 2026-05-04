package edu.cit.resuera.eventhive.event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.resuera.eventhive.event.Event;
import edu.cit.resuera.eventhive.event.EventStatus;
import edu.cit.resuera.eventhive.user.User;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizer(User organizer);
    List<Event> findByStatus(EventStatus status);
    List<Event> findByCategory(String category);
    List<Event> findByOrganizerAndStatus(User organizer, EventStatus status);
}