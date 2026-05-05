package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // 🔥 ĐÃ SỬA: Dùng dấu gạch dưới để trỏ vào ID của Quiz
    List<Question> findByQuiz_Id(Long quizId);

    @Query("SELECT q FROM Question q " +
            "JOIN q.course c " +
            "JOIN c.instructor i " +
            "JOIN i.user u " +
            "WHERE u.email = :email " +
            "ORDER BY q.createdAt DESC")
    List<Question> findAllByInstructorEmail(String email);
}