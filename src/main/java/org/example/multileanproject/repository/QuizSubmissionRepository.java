package org.example.multileanproject.repository;
import org.example.multileanproject.entity.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long>{
    // Tìm lịch sử nộp bài của một lần học cụ thể (Enrollment) cho 1 bài Quiz
    List<QuizSubmission> findByEnrollmentIdAndQuizId(Long enrollmentId, Long quizId);

    // Đếm số lần đã nộp — dùng để giới hạn retry
    long countByEnrollmentIdAndQuizId(Long enrollmentId, Long quizId);
}
