package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.multileanproject.entity.AdminActionLog;
import org.example.multileanproject.entity.Notification;
import org.example.multileanproject.repository.AdminActionLogRepository;
import org.example.multileanproject.repository.NotificationRepository;
import org.example.multileanproject.util.TextEncodingUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class EncodingRepairService {

    private final NotificationRepository notificationRepository;
    private final AdminActionLogRepository adminActionLogRepository;

    @Value("${app.encoding.repair-on-startup:true}")
    private boolean repairOnStartup;

    @Value("${app.encoding.repair-batch-size:2000}")
    private int repairBatchSize;

    @EventListener(ApplicationReadyEvent.class)
    public void repairMojibakeData() {
        if (!repairOnStartup) {
            return;
        }

        int fixedNotifications = repairNotifications();
        int fixedAdminLogs = repairAdminLogs();

        if (fixedNotifications > 0 || fixedAdminLogs > 0) {
            log.info(
                    "Encoding repair completed: notificationsFixed={}, adminLogsFixed={}",
                    fixedNotifications,
                    fixedAdminLogs
            );
        }
    }

    private int repairNotifications() {
        List<Notification> notifications = notificationRepository
                .findAll(PageRequest.of(0, Math.max(repairBatchSize, 1), Sort.by(Sort.Direction.DESC, "id")))
                .getContent();

        List<Notification> changed = new ArrayList<>();

        for (Notification n : notifications) {
            if (n == null) {
                continue;
            }

            String normalizedCourseName = normalize(n.getCourseName());
            String normalizedStudentName = normalize(n.getStudentName());
            String fixedTitle = fallbackNotificationTitle(n, normalize(n.getTitle()), normalizedCourseName);
            String fixedMessage = fallbackNotificationMessage(n, normalize(n.getMessage()), normalizedCourseName, normalizedStudentName);

            boolean dirty = false;
            if (!Objects.equals(n.getTitle(), fixedTitle)) {
                n.setTitle(fixedTitle);
                dirty = true;
            }
            if (!Objects.equals(n.getMessage(), fixedMessage)) {
                n.setMessage(fixedMessage);
                dirty = true;
            }
            if (!Objects.equals(n.getCourseName(), normalizedCourseName)) {
                n.setCourseName(normalizedCourseName);
                dirty = true;
            }
            if (!Objects.equals(n.getStudentName(), normalizedStudentName)) {
                n.setStudentName(normalizedStudentName);
                dirty = true;
            }

            if (dirty) {
                changed.add(n);
            }
        }

        if (!changed.isEmpty()) {
            notificationRepository.saveAll(changed);
        }

        return changed.size();
    }

    private int repairAdminLogs() {
        List<AdminActionLog> logs = adminActionLogRepository
                .findAll(PageRequest.of(0, Math.max(repairBatchSize, 1), Sort.by(Sort.Direction.DESC, "id")))
                .getContent();

        List<AdminActionLog> changed = new ArrayList<>();

        for (AdminActionLog logEntry : logs) {
            if (logEntry == null) {
                continue;
            }

            String fixedAdmin = normalize(logEntry.getAdminUsername());
            String fixedAction = normalize(logEntry.getAction());
            String fixedDescription = normalize(logEntry.getDescription());
            String fixedTargetType = normalize(logEntry.getTargetType());

            boolean dirty = false;
            if (!Objects.equals(logEntry.getAdminUsername(), fixedAdmin)) {
                logEntry.setAdminUsername(fixedAdmin);
                dirty = true;
            }
            if (!Objects.equals(logEntry.getAction(), fixedAction)) {
                logEntry.setAction(fixedAction);
                dirty = true;
            }
            if (!Objects.equals(logEntry.getDescription(), fixedDescription)) {
                logEntry.setDescription(fixedDescription);
                dirty = true;
            }
            if (!Objects.equals(logEntry.getTargetType(), fixedTargetType)) {
                logEntry.setTargetType(fixedTargetType);
                dirty = true;
            }

            if (dirty) {
                changed.add(logEntry);
            }
        }

        if (!changed.isEmpty()) {
            adminActionLogRepository.saveAll(changed);
        }

        return changed.size();
    }

    private String fallbackNotificationTitle(Notification n, String normalizedTitle, String normalizedCourseName) {
        if (!looksBrokenQuestionMarks(normalizedTitle)) {
            return normalizedTitle;
        }

        String type = n.getType();
        if ("COURSE_BLOCKED".equals(type)) {
            return "KhÃ³a há»c bá»‹ táº¡m khÃ³a";
        }
        if ("INSTRUCTOR_COURSE_APPROVED".equals(type)) {
            return "KhÃ³a há»c Ä‘Ã£ Ä‘Æ°á»£c duyá»‡t";
        }
        if ("INSTRUCTOR_COURSE_REJECTED".equals(type)) {
            return "KhÃ³a há»c bá»‹ tá»« chá»‘i";
        }
        if ("INSTRUCTOR_COURSE_STATUS_UPDATED".equals(type)) {
            return "Cáº­p nháº­t tráº¡ng thÃ¡i khÃ³a há»c";
        }
        if ("INSTRUCTOR_COURSE_DELETED".equals(type)) {
            return "Khoa hoc da bi xoa";
        }
        if ("STUDENT_PAYMENT_SUCCESS".equals(type)) {
            return "Thanh toÃ¡n thÃ nh cÃ´ng";
        }
        if ("INSTRUCTOR_NEW_ENROLLMENT".equals(type)) {
            return "CÃ³ há»c viÃªn má»›i Ä‘Äƒng kÃ½";
        }
        if ("INSTRUCTOR_NEW_QUESTION".equals(type)) {
            return "CÃ³ cÃ¢u há»i má»›i";
        }
        if ("NEW_LESSON".equals(type) && normalizedCourseName != null && !normalizedCourseName.isBlank()) {
            return "BÃ i há»c má»›i trong khÃ³a " + normalizedCourseName;
        }
        return normalizedTitle;
    }

    private String fallbackNotificationMessage(
            Notification n,
            String normalizedMessage,
            String normalizedCourseName,
            String normalizedStudentName
    ) {
        if (!looksBrokenQuestionMarks(normalizedMessage)) {
            return normalizedMessage;
        }

        String courseName = (normalizedCourseName == null || normalizedCourseName.isBlank())
                ? "khÃ³a há»c"
                : "\"" + normalizedCourseName + "\"";
        String studentName = (normalizedStudentName == null || normalizedStudentName.isBlank())
                ? "Há»c viÃªn"
                : normalizedStudentName;

        String type = n.getType();
        if ("COURSE_BLOCKED".equals(type)) {
            return "KhÃ³a há»c " + courseName + " hiá»‡n Ä‘Ã£ bá»‹ táº¡m khÃ³a. Tiáº¿n trÃ¬nh há»c cá»§a báº¡n váº«n Ä‘Æ°á»£c lÆ°u.";
        }
        if ("INSTRUCTOR_COURSE_APPROVED".equals(type)) {
            return "KhÃ³a há»c " + courseName + " Ä‘Ã£ Ä‘Æ°á»£c admin duyá»‡t vÃ  Ä‘á»‹nh giÃ¡.";
        }
        if ("INSTRUCTOR_COURSE_REJECTED".equals(type)) {
            return "KhÃ³a há»c " + courseName + " bá»‹ tá»« chá»‘i. Vui lÃ²ng kiá»ƒm tra láº¡i ná»™i dung.";
        }
        if ("INSTRUCTOR_COURSE_STATUS_UPDATED".equals(type)) {
            return "KhÃ³a há»c Ä‘Ã£ thay Ä‘á»•i tráº¡ng thÃ¡i.";
        }
        if ("INSTRUCTOR_COURSE_DELETED".equals(type)) {
            return "Khoa hoc " + courseName + " da bi admin xoa khoi he thong.";
        }
        if ("STUDENT_PAYMENT_SUCCESS".equals(type)) {
            return "Báº¡n Ä‘Ã£ Ä‘Äƒng kÃ½ thÃ nh cÃ´ng khÃ³a há»c " + courseName + ".";
        }
        if ("INSTRUCTOR_NEW_ENROLLMENT".equals(type)) {
            return "Há»c viÃªn " + studentName + " vá»«a mua khÃ³a há»c cá»§a báº¡n.";
        }
        if ("INSTRUCTOR_NEW_QUESTION".equals(type)) {
            return "Há»c viÃªn " + studentName + " vá»«a Ä‘áº·t cÃ¢u há»i trong khÃ³a " + courseName + ".";
        }
        return normalizedMessage;
    }

    private String normalize(String value) {
        return TextEncodingUtil.normalize(value);
    }

    private boolean looksBrokenQuestionMarks(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        long questionMarks = value.chars().filter(ch -> ch == '?').count();
        return questionMarks >= 3;
    }
}

