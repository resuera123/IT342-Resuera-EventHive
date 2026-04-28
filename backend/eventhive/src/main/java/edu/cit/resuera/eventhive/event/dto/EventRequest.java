package edu.cit.resuera.eventhive.event.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EventRequest {
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private String category;
    private Integer maxParticipants;
}