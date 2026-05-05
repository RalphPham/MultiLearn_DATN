package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.entity.Notification;
import org.example.multileanproject.repository.NotificationRepository;
import org.example.multileanproject.util.TextEncodingUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void createNotification(
            Long userId,
            String title,
            String message,
            String targetUrl,
            String type,
            Notification.NotificationCategory category,
            boolean isImportant,
            String courseName,
            String studentName
    ) {
        String normalizedTitle = TextEncodingUtil.normalize(title);
        String normalizedMessage = TextEncodingUtil.normalize(message);
        String normalizedCourseName = TextEncodingUtil.normalize(courseName);
        String normalizedStudentName = TextEncodingUtil.normalize(studentName);

        Notification notification = Notification.builder()
                .userId(userId)
                .title(normalizedTitle)
                .message(normalizedMessage)
                .targetUrl(targetUrl)
                .type(type == null || type.isBlank() ? "SYSTEM" : type)
                .category(category == null ? Notification.NotificationCategory.SYSTEM_NOTIFICATION : category)
                .isImportant(isImportant)
                .courseName(normalizedCourseName)
                .studentName(normalizedStudentName)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void createNotification(
            String title,
            String message,
            String type,
            String targetUrl,
            Long userId,
            String courseName
    ) {
        createNotification(
                userId,
                title,
                message,
                targetUrl,
                type,
                Notification.NotificationCategory.SYSTEM_NOTIFICATION,
                false,
                courseName,
                null
        );
    }

    @Override
    public List<Notification> getRecentNotifications(Long userId) {
        return notificationRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public Page<Notification> searchNotifications(
            Long userId,
            Boolean isImportant,
            Notification.NotificationCategory category,
            String courseName,
            String studentName,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        return notificationRepository.searchNotifications(
                userId, isImportant, category, courseName, studentName, startDate, endDate, pageable
        );
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(notifications);
    }
}
