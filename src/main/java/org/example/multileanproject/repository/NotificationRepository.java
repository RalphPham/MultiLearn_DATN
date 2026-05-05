package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndIsReadFalse(Long userId);

    List<Notification> findByUserIdAndIsReadFalse(Long userId);

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    @Query("""
    SELECT n
    FROM Notification n
    WHERE n.userId = :userId
      AND (:isImportant IS NULL OR n.isImportant = :isImportant)
      AND (:category IS NULL OR n.category = :category)
      AND (:courseName IS NULL OR LOWER(n.courseName) LIKE LOWER(CONCAT('%', :courseName, '%')))
      AND (:studentName IS NULL OR LOWER(n.studentName) LIKE LOWER(CONCAT('%', :studentName, '%')))
      AND (:startDate IS NULL OR n.createdAt >= :startDate)
      AND (:endDate IS NULL OR n.createdAt <= :endDate)
""")
    Page<Notification> searchNotifications(
            @Param("userId") Long userId,
            @Param("isImportant") Boolean isImportant,
            @Param("category") Notification.NotificationCategory category,
            @Param("courseName") String courseName,
            @Param("studentName") String studentName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    // --- 3 HÀM DÀNH RIÊNG CHO CHUÔNG HỌC VIÊN (Trang chủ) ---
    // Đếm thông báo Học viên (Bỏ qua các thông báo bắt đầu bằng chữ INSTRUCTOR_)
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.type NOT LIKE 'INSTRUCTOR_%'")
    long countUnreadStudentNotifications(@Param("userId") Long userId);

    // Lấy thông báo Học viên
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.type NOT LIKE 'INSTRUCTOR_%' ORDER BY n.createdAt DESC")
    List<Notification> findRecentStudentNotifications(@Param("userId") Long userId, Pageable pageable);

    // Đọc tất cả thông báo Học viên
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false AND n.type NOT LIKE 'INSTRUCTOR_%'")
    void markAllStudentAsRead(@Param("userId") Long userId);


    // --- 3 HÀM DÀNH RIÊNG CHO CHUÔNG GIẢNG VIÊN (Trong Studio) ---
    // Đếm thông báo Giảng viên (Chỉ lấy các thông báo bắt đầu bằng chữ INSTRUCTOR_)
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.type LIKE 'INSTRUCTOR_%'")
    long countUnreadInstructorNotifications(@Param("userId") Long userId);

    // Lấy thông báo Giảng viên
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.type LIKE 'INSTRUCTOR_%' ORDER BY n.createdAt DESC")
    List<Notification> findRecentInstructorNotifications(@Param("userId") Long userId, Pageable pageable);

    // Đọc tất cả thông báo Giảng viên
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false AND n.type LIKE 'INSTRUCTOR_%'")
    void markAllInstructorAsRead(@Param("userId") Long userId);
}