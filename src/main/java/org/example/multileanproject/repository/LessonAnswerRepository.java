package org.example.multileanproject.repository;

import org.example.multileanproject.entity.LessonAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonAnswerRepository extends JpaRepository<LessonAnswer, Long> {
}