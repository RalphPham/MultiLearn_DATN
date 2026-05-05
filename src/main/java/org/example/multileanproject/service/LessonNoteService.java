package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.NoteCreateRequest;
import org.example.multileanproject.dto.NoteDTO;
import org.example.multileanproject.dto.NoteUpdateRequest;
import org.example.multileanproject.entity.Lesson;
import org.example.multileanproject.entity.Note;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.repository.EnrollmentRepository;
import org.example.multileanproject.repository.LessonRepository;
import org.example.multileanproject.repository.NoteRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonNoteService {

    private final NoteRepository noteRepository;
    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    private Student getCurrentStudent() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Khong tim thay thong tin hoc vien."));
    }

    private Lesson validateLessonAccess(Student student, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay bai hoc."));

        Long courseId = lesson.getSection().getCourse().getId();
        boolean enrolled = enrollmentRepository.existsByStudent_IdAndCourse_IdAndStatus(
                student.getId(),
                courseId,
                "ACTIVE"
        );
        if (!enrolled) {
            throw new RuntimeException("Ban chua dang ky khoa hoc nay.");
        }
        return lesson;
    }

    @Transactional(readOnly = true)
    public List<NoteDTO> getNotesByLesson(Long lessonId) {
        Student student = getCurrentStudent();
        validateLessonAccess(student, lessonId);

        return noteRepository.findByStudentIdAndLessonIdOrderByCreatedAtDesc(student.getId(), lessonId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public NoteDTO createNote(NoteCreateRequest request) {
        if (request == null || request.getLessonId() == null) {
            throw new RuntimeException("Thieu lessonId.");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new RuntimeException("Noi dung ghi chu khong duoc de trong.");
        }

        Student student = getCurrentStudent();
        validateLessonAccess(student, request.getLessonId());

        Note note = Note.builder()
                .studentId(student.getId())
                .lessonId(request.getLessonId())
                .content(request.getContent().trim())
                .timestampSeconds(normalizeTimestamp(request.getTimestampSeconds()))
                .build();

        return toDTO(noteRepository.save(note));
    }

    @Transactional
    public NoteDTO updateNote(Long noteId, NoteUpdateRequest request) {
        if (request == null || request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new RuntimeException("Noi dung ghi chu khong duoc de trong.");
        }

        Student student = getCurrentStudent();
        Note note = noteRepository.findByIdAndStudentId(noteId, student.getId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay ghi chu."));

        validateLessonAccess(student, note.getLessonId());
        note.setContent(request.getContent().trim());
        // Cho phép cập nhật về null để "bỏ mốc" và gắn lại theo mốc mới.
        note.setTimestampSeconds(normalizeTimestamp(request.getTimestampSeconds()));

        return toDTO(noteRepository.save(note));
    }

    @Transactional
    public void deleteNote(Long noteId) {
        Student student = getCurrentStudent();
        Note note = noteRepository.findByIdAndStudentId(noteId, student.getId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay ghi chu."));

        validateLessonAccess(student, note.getLessonId());
        noteRepository.delete(note);
    }

    private NoteDTO toDTO(Note note) {
        return NoteDTO.builder()
                .id(note.getId())
                .lessonId(note.getLessonId())
                .content(note.getContent())
                .timestampSeconds(note.getTimestampSeconds())
                .createdAt(note.getCreatedAt())
                .build();
    }

    private Integer normalizeTimestamp(Integer timestampSeconds) {
        if (timestampSeconds == null) return null;
        return Math.max(0, timestampSeconds);
    }
}
