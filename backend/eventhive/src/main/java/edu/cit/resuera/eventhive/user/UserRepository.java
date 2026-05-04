package edu.cit.resuera.eventhive.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.resuera.eventhive.user.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}