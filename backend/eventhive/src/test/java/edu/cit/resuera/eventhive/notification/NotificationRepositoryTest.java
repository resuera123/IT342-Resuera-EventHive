package edu.cit.resuera.eventhive.notification;

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
class NotificationRepositoryTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private NotificationRepository notificationRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("u@example.com");
        user.setFirstname("U"); user.setLastname("Ser");
        user.setPasswordHash("h"); user.setRole("participant");
        user.setCreatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(user);
    }

    @Test
    @DisplayName("findByUserOrderByCreatedAtDesc returns notifications newest-first")
    void findByUserOrderByCreatedAtDesc_orderedNewestFirst() {
        Notification older = persistNotification("REGISTRATION", "Older",
                LocalDateTime.now().minusHours(2), false);
        Notification newer = persistNotification("EVENT_CANCELLED", "Newer",
                LocalDateTime.now().minusHours(1), false);
        entityManager.flush();

        List<Notification> result = notificationRepository.findByUserOrderByCreatedAtDesc(user);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(newer.getId());
        assertThat(result.get(1).getId()).isEqualTo(older.getId());
    }

    @Test
    @DisplayName("findByUserAndReadFalse returns only unread notifications")
    void findByUserAndReadFalse_excludesRead() {
        persistNotification("REGISTRATION", "Read one", LocalDateTime.now(), true);
        persistNotification("EVENT_CANCELLED", "Unread one", LocalDateTime.now(), false);
        persistNotification("NEW_PARTICIPANT", "Unread two", LocalDateTime.now(), false);
        entityManager.flush();

        List<Notification> unread = notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);

        assertThat(unread).hasSize(2);
        assertThat(unread).allMatch(n -> !n.isRead());
    }

    @Test
    @DisplayName("countByUserAndReadFalse returns the number of unread notifications")
    void countByUserAndReadFalse_returnsCount() {
        persistNotification("R", "1", LocalDateTime.now(), false);
        persistNotification("R", "2", LocalDateTime.now(), false);
        persistNotification("R", "3", LocalDateTime.now(), true);
        entityManager.flush();

        int count = notificationRepository.countByUserAndReadFalse(user);

        assertThat(count).isEqualTo(2);
    }

    private Notification persistNotification(String type, String title, LocalDateTime createdAt, boolean read) {
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setTitle(title);
        n.setMessage("Message body");
        n.setRead(read);
        n.setCreatedAt(createdAt);
        n.setEventId(1L);
        entityManager.persist(n);
        return n;
    }
}