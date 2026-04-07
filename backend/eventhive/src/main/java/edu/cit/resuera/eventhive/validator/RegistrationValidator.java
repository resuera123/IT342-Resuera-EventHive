package edu.cit.resuera.eventhive.validator;

import edu.cit.resuera.eventhive.entity.Event;
import edu.cit.resuera.eventhive.entity.User;

public interface RegistrationValidator {

    void validate(User user, Event event);
}