package edu.cit.resuera.eventhive.validator;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import edu.cit.resuera.eventhive.entity.Event;
import edu.cit.resuera.eventhive.entity.EventStatus;
import edu.cit.resuera.eventhive.entity.User;
import edu.cit.resuera.eventhive.repository.EventRegistrationRepository;

/**
 * Strategy 1: Organizers cannot register for their own events.
 */
@Component
@Order(1)
class NotOrganizerValidator implements RegistrationValidator {

    @Override
    public void validate(User user, Event event) {
        if (event.getOrganizer().getId().equals(user.getId())) {
            throw new RuntimeException("Organizers cannot register for their own events");
        }
    }
}

/**
 * Strategy 2: Cannot register for cancelled events.
 */
@Component
@Order(2)
class NotCancelledValidator implements RegistrationValidator {

    @Override
    public void validate(User user, Event event) {
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new RuntimeException("Cannot register for a cancelled event");
        }
    }
}

/**
 * Strategy 3: Cannot register twice for the same event.
 */
@Component
@Order(3)
class NotDuplicateValidator implements RegistrationValidator {

    private final EventRegistrationRepository registrationRepository;

    NotDuplicateValidator(EventRegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    @Override
    public void validate(User user, Event event) {
        if (registrationRepository.existsByUserAndEvent(user, event)) {
            throw new RuntimeException("Already registered for this event");
        }
    }
}

/**
 * Strategy 4: Event must not be at full capacity.
 */
@Component
@Order(4)
class CapacityValidator implements RegistrationValidator {

    private final EventRegistrationRepository registrationRepository;

    CapacityValidator(EventRegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    @Override
    public void validate(User user, Event event) {
        if (event.getMaxParticipants() != null) {
            int current = registrationRepository.countByEvent(event);
            if (current >= event.getMaxParticipants()) {
                throw new RuntimeException("Event is full");
            }
        }
    }
}