package edu.cit.resuera.eventhive.dto;

import java.time.LocalDateTime;

import edu.cit.resuera.eventhive.entity.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private String category;
    private String imageUrl;
    private Integer maxParticipants;
    private EventStatus status;
    private Long organizerId;
    private String organizerName;
    private LocalDateTime createdAt;
    private Integer participantCount;
    private Boolean isRegistered;
}