package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByStudentIdAndLessonIdOrderByCreatedAtDesc(Long studentId, Long lessonId);
    Optional<Note> findByIdAndStudentId(Long noteId, Long studentId);
}
