package edu.cit.resuera.eventhive.event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.resuera.eventhive.event.Event;
import edu.cit.resuera.eventhive.event.EventRegistration;
import edu.cit.resuera.eventhive.user.User;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    boolean existsByUserAndEvent(User user, Event event);

    int countByEvent(Event event);

    List<EventRegistration> findByUser(User user);

    List<EventRegistration> findByEvent(Event event);
}