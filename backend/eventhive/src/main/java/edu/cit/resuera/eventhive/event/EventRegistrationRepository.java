package edu.cit.resuera.eventhive.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.resuera.eventhive.entity.Event;
import edu.cit.resuera.eventhive.entity.EventRegistration;
import edu.cit.resuera.eventhive.entity.User;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    boolean existsByUserAndEvent(User user, Event event);

    int countByEvent(Event event);

    List<EventRegistration> findByUser(User user);

    List<EventRegistration> findByEvent(Event event);
}