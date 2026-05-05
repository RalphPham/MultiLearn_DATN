package org.example.multileanproject.repository;

import org.example.multileanproject.entity.LessonQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LessonQuestionRepository extends JpaRepository<LessonQuestion, Long> {
    List<LessonQuestion> findByLessonIdOrderByCreatedAtDesc(Long lessonId);
    List<LessonQuestion> findByCourseIdInOrderByCreatedAtDesc(List<Long> courseIds);
}