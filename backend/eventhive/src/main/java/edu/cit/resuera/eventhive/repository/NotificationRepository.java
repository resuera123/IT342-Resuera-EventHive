package edu.cit.resuera.eventhive.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.resuera.eventhive.entity.Notification;
import edu.cit.resuera.eventhive.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(User user);

    int countByUserAndReadFalse(User user);
}