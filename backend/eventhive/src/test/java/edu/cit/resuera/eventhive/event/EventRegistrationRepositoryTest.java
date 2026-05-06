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
class EventRegistrationRepositoryTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private EventRegistrationRepository registrationRepository;

    private User participant;
    private Event event;

    @BeforeEach
    void setUp() {
        User organizer = new User();
        organizer.setEmail("o@example.com");
        organizer.setFirstname("Org"); organizer.setLastname("Anizer");
        organizer.setPasswordHash("h"); organizer.setRole("organizer");
        organizer.setCreatedAt(LocalDateTime.now());
        entityManager.persist(organizer);

        participant = new User();
        participant.setEmail("p@example.com");
        participant.setFirstname("Part"); participant.setLastname("Icipant");
        participant.setPasswordHash("h"); participant.setRole("participant");
        participant.setCreatedAt(LocalDateTime.now());
        entityManager.persist(participant);

        event = new Event();
        event.setTitle("Test Event");
        event.setDescription("d");
        event.setStartDate(LocalDateTime.now().plusDays(1));
        event.setEndDate(LocalDateTime.now().plusDays(2));
        event.setLocation("Online"); event.setCategory("Tech");
        event.setMaxParticipants(10); event.setStatus(EventStatus.UPCOMING);
        event.setOrganizer(organizer); event.setCreatedAt(LocalDateTime.now());
        entityManager.persist(event);

        entityManager.flush();
    }

    @Test
    @DisplayName("existsByUserAndEvent returns false before registration, true after")
    void existsByUserAndEvent_reflectsRegistration() {
        assertThat(registrationRepository.existsByUserAndEvent(participant, event)).isFalse();

        EventRegistration reg = new EventRegistration();
        reg.setUser(participant);
        reg.setEvent(event);
        reg.setRegisteredAt(LocalDateTime.now());
        registrationRepository.save(reg);
        entityManager.flush();

        assertThat(registrationRepository.existsByUserAndEvent(participant, event)).isTrue();
    }

    @Test
    @DisplayName("countByEvent returns 0 with no registrations and increments correctly")
    void countByEvent_incrementsWithRegistrations() {
        assertThat(registrationRepository.countByEvent(event)).isEqualTo(0);

        User another = new User();
        another.setEmail("other@example.com");
        another.setFirstname("An"); another.setLastname("Other");
        another.setPasswordHash("h"); another.setRole("participant");
        another.setCreatedAt(LocalDateTime.now());
        entityManager.persist(another);

        EventRegistration r1 = new EventRegistration();
        r1.setUser(participant); r1.setEvent(event); r1.setRegisteredAt(LocalDateTime.now());
        EventRegistration r2 = new EventRegistration();
        r2.setUser(another); r2.setEvent(event); r2.setRegisteredAt(LocalDateTime.now());
        registrationRepository.save(r1);
        registrationRepository.save(r2);
        entityManager.flush();

        assertThat(registrationRepository.countByEvent(event)).isEqualTo(2);
    }

    @Test
    @DisplayName("findByUser returns only registrations owned by that user")
    void findByUser_scopesToOwner() {
        EventRegistration reg = new EventRegistration();
        reg.setUser(participant);
        reg.setEvent(event);
        reg.setRegisteredAt(LocalDateTime.now());
        registrationRepository.save(reg);
        entityManager.flush();

        List<EventRegistration> list = registrationRepository.findByUser(participant);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getUser().getEmail()).isEqualTo("p@example.com");
    }

    @Test
    @DisplayName("findByEvent returns all registrations for the event")
    void findByEvent_returnsAllForEvent() {
        EventRegistration reg = new EventRegistration();
        reg.setUser(participant);
        reg.setEvent(event);
        reg.setRegisteredAt(LocalDateTime.now());
        registrationRepository.save(reg);
        entityManager.flush();

        List<EventRegistration> list = registrationRepository.findByEvent(event);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getEvent().getId()).isEqualTo(event.getId());
    }
}