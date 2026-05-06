package edu.cit.resuera.eventhive.event;

import edu.cit.resuera.eventhive.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class EventRepositoryTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private EventRepository eventRepository;

    private User organizer;

    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setEmail("organizer@example.com");
        organizer.setFirstname("Olivia");
        organizer.setLastname("Organizer");
        organizer.setPasswordHash("h");
        organizer.setRole("organizer");
        organizer.setCreatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(organizer);
    }

    @Test
    @DisplayName("findByOrganizer returns only events created by that organizer")
    void findByOrganizer_filtersByOrganizer() {
        User otherOrganizer = new User();
        otherOrganizer.setEmail("other@example.com");
        otherOrganizer.setFirstname("Other");
        otherOrganizer.setLastname("Org");
        otherOrganizer.setPasswordHash("h");
        otherOrganizer.setRole("organizer");
        otherOrganizer.setCreatedAt(LocalDateTime.now());
        entityManager.persist(otherOrganizer);

        persistEvent("Mine 1", organizer, EventStatus.UPCOMING, "Tech");
        persistEvent("Mine 2", organizer, EventStatus.ONGOING, "Music");
        persistEvent("Theirs", otherOrganizer, EventStatus.UPCOMING, "Sports");
        entityManager.flush();

        List<Event> mine = eventRepository.findByOrganizer(organizer);

        assertThat(mine).hasSize(2);
        assertThat(mine).extracting(Event::getTitle).containsExactlyInAnyOrder("Mine 1", "Mine 2");
    }

    @Test
    @DisplayName("findByStatus returns only events with the given status")
    void findByStatus_filtersByStatus() {
        persistEvent("A", organizer, EventStatus.UPCOMING, "Tech");
        persistEvent("B", organizer, EventStatus.CANCELLED, "Tech");
        persistEvent("C", organizer, EventStatus.UPCOMING, "Music");
        entityManager.flush();

        List<Event> upcoming = eventRepository.findByStatus(EventStatus.UPCOMING);

        assertThat(upcoming).hasSize(2);
        assertThat(upcoming).allMatch(e -> e.getStatus() == EventStatus.UPCOMING);
    }

    @Test
    @DisplayName("findByCategory returns only events in that category")
    void findByCategory_filtersByCategory() {
        persistEvent("Tech 1", organizer, EventStatus.UPCOMING, "Tech");
        persistEvent("Music 1", organizer, EventStatus.UPCOMING, "Music");
        persistEvent("Tech 2", organizer, EventStatus.UPCOMING, "Tech");
        entityManager.flush();

        List<Event> tech = eventRepository.findByCategory("Tech");

        assertThat(tech).hasSize(2);
        assertThat(tech).extracting(Event::getCategory).containsOnly("Tech");
    }

    @Test
    @DisplayName("findByOrganizerAndStatus combines both filters")
    void findByOrganizerAndStatus_combinesFilters() {
        persistEvent("OK1", organizer, EventStatus.UPCOMING, "Tech");
        persistEvent("OK2", organizer, EventStatus.UPCOMING, "Music");
        persistEvent("Skip", organizer, EventStatus.CANCELLED, "Tech");
        entityManager.flush();

        List<Event> result = eventRepository.findByOrganizerAndStatus(organizer, EventStatus.UPCOMING);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Event::getStatus).containsOnly(EventStatus.UPCOMING);
    }

    private void persistEvent(String title, User org, EventStatus status, String category) {
        Event e = new Event();
        e.setTitle(title);
        e.setDescription("desc");
        e.setStartDate(LocalDateTime.now().plusDays(1));
        e.setEndDate(LocalDateTime.now().plusDays(2));
        e.setLocation("Online");
        e.setCategory(category);
        e.setMaxParticipants(50);
        e.setStatus(status);
        e.setOrganizer(org);
        e.setCreatedAt(LocalDateTime.now());
        entityManager.persist(e);
    }
}