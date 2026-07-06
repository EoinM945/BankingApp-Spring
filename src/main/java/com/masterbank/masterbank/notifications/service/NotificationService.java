package com.masterbank.masterbank.notifications.service;

import com.masterbank.masterbank.authUsers.entity.User;
import com.masterbank.masterbank.notifications.dtos.NotificationDTO;

public interface NotificationService {
    void sendEmail(NotificationDTO notificationDTO, User user);
}
