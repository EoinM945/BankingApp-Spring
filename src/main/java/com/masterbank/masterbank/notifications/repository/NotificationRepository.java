package com.masterbank.masterbank.notifications.repository;

import com.masterbank.masterbank.notifications.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
