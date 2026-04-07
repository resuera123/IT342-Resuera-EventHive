package edu.cit.resuera.eventhive.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import edu.cit.resuera.eventhive.dto.EventRequest;
import edu.cit.resuera.eventhive.dto.EventResponse;
import edu.cit.resuera.eventhive.entity.EventStatus;

public interface IEventService {

    EventResponse createEvent(EventRequest request, String organizerEmail);

    EventResponse createEvent(EventRequest request, String organizerEmail, MultipartFile image) throws IOException;

    EventResponse updateEvent(Long eventId, EventRequest request, String organizerEmail, MultipartFile image) throws IOException;

    EventResponse updateEventStatus(Long eventId, EventStatus status);

    void deleteEvent(Long eventId);

    List<EventResponse> getAllEvents();

    List<EventResponse> getAllEventsForUser(String email);

    List<EventResponse> getEventsByOrganizer(String organizerEmail);

    List<EventResponse> getEventsByCategory(String category);

    EventResponse registerForEvent(Long eventId, String userEmail);

    List<EventResponse> getRegisteredEvents(String userEmail);
}