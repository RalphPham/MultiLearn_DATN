package org.example.multileanproject.repository;

import org.example.multileanproject.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Lấy lịch sử chat giữa 2 người, sắp xếp từ cũ đến mới
    @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = :user1 AND m.receiverId = :user2) " +
            "OR (m.senderId = :user2 AND m.receiverId = :user1) ORDER BY m.timestamp ASC")
    List<ChatMessage> findConversation(@Param("user1") Long user1, @Param("user2") Long user2);

    // Lấy lịch sử chat có phân trang (mới nhất trước, frontend đảo lại)
    @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = :user1 AND m.receiverId = :user2) " +
            "OR (m.senderId = :user2 AND m.receiverId = :user1) ORDER BY m.timestamp DESC")
    List<ChatMessage> findConversationPaged(@Param("user1") Long user1, @Param("user2") Long user2, Pageable pageable);

    // Lấy danh sách liên hệ (Lấy ID của tin nhắn mới nhất trong mỗi cuộc hội thoại)
    @Query("SELECT m FROM ChatMessage m WHERE m.id IN (" +
            "SELECT MAX(m2.id) FROM ChatMessage m2 WHERE m2.senderId = :userId OR m2.receiverId = :userId " +
            "GROUP BY CASE WHEN m2.senderId = :userId THEN m2.receiverId ELSE m2.senderId END) " +
            "ORDER BY m.timestamp DESC")
    List<ChatMessage> findLatestMessagesByUser(@Param("userId") Long userId);
    // Đếm tổng tin nhắn chưa đọc của một user
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiverId = :userId AND m.isRead = false")
    long countUnreadMessages(@Param("userId") Long userId);

    // Đếm tin nhắn chưa đọc từ một partner cụ thể
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.senderId = :senderId AND m.receiverId = :receiverId AND m.isRead = false")
    long countUnreadFromPartner(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    // Đánh dấu toàn bộ tin nhắn người khác gửi cho mình thành "Đã đọc"
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.senderId = :senderId AND m.receiverId = :receiverId AND m.isRead = false")
    void markMessagesAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}