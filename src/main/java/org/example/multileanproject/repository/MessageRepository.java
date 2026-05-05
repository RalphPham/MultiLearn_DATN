package org.example.multileanproject.repository;

import org.example.multileanproject.dto.ContactDTO;
import org.example.multileanproject.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Lấy lịch sử chat giữa 2 người dùng (Giảng viên và Học viên)
    @Query("SELECT m FROM Message m WHERE " +
            "(m.senderId = :id1 AND m.receiverId = :id2) OR " +
            "(m.senderId = :id2 AND m.receiverId = :id1) " +
            "ORDER BY m.createdAt ASC")
    List<Message> findChatHistory(@Param("id1") Long id1, @Param("id2") Long id2);

    // Lấy danh sách liên hệ và tin nhắn cuối cùng (Query mẫu tối giản)
    @Query("SELECT new org.example.multileanproject.dto.ContactDTO(m.senderId, 'Học viên', m.content, m.createdAt) " +
            "FROM Message m WHERE m.receiverId = :instructorId " +
            "AND m.id IN (SELECT MAX(m2.id) FROM Message m2 WHERE m2.receiverId = :instructorId GROUP BY m2.senderId)")
    List<ContactDTO> findContactsForInstructor(@Param("instructorId") Long instructorId);
}