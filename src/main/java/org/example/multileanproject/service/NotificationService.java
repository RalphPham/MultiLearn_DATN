package org.example.multileanproject.service;

import org.example.multileanproject.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationService {

    void createNotification(
            Long userId,
            String title,
            String message,
            String targetUrl,
            String type,
            Notification.NotificationCategory category,
            boolean isImportant,
            String courseName,
            String studentName
    );

    // overload để tương thích code cũ
    void createNotification(
            String title,
            String message,
            String type,
            String targetUrl,
            Long userId,
            String courseName
    );

    List<Notification> getRecentNotifications(Long userId);

    long countUnread(Long userId);

    Page<Notification> searchNotifications(
            Long userId,
            Boolean isImportant,
            Notification.NotificationCategory category,
            String courseName,
            String studentName,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);
}