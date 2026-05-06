package edu.cit.resuera.eventhive.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @DataJpaTest spins up an in-memory H2 database and only loads JPA-related beans.
 * AutoConfigureTestDatabase.Replace.ANY ensures we use H2 even though prod uses
 * PostgreSQL/MySQL.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private UserRepository userRepository;

    @Test
    @DisplayName("findByEmail returns the user when the email exists")
    void findByEmail_existingUser_returnsUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstname("Test");
        user.setLastname("User");
        user.setPasswordHash("hashed");
        user.setRole("participant");
        user.setCreatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstname()).isEqualTo("Test");
    }

    @Test
    @DisplayName("findByEmail returns empty for a nonexistent email")
    void findByEmail_unknownEmail_returnsEmpty() {
        Optional<User> found = userRepository.findByEmail("ghost@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail returns true when email exists, false otherwise")
    void existsByEmail_returnsCorrectly() {
        User user = new User();
        user.setEmail("exists@example.com");
        user.setFirstname("Real");
        user.setLastname("User");
        user.setPasswordHash("h");
        user.setRole("participant");
        user.setCreatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(user);

        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("ghost@example.com")).isFalse();
    }
}