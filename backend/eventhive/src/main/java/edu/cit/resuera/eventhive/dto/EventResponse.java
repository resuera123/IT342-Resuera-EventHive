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

    // Builder Pattern

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
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

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder startDate(LocalDateTime startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDateTime endDate) { this.endDate = endDate; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public Builder maxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; return this; }
        public Builder status(EventStatus status) { this.status = status; return this; }
        public Builder organizerId(Long organizerId) { this.organizerId = organizerId; return this; }
        public Builder organizerName(String organizerName) { this.organizerName = organizerName; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder participantCount(Integer participantCount) { this.participantCount = participantCount; return this; }
        public Builder isRegistered(Boolean isRegistered) { this.isRegistered = isRegistered; return this; }
 
        public EventResponse build() {
            return new EventResponse(id, title, description, startDate, endDate,
                    location, category, imageUrl, maxParticipants, status,
                    organizerId, organizerName, createdAt, participantCount, isRegistered);
        }
    }
}