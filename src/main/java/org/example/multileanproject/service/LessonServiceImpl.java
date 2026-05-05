package org.example.multileanproject.service;



import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.LessonRequest;
import org.example.multileanproject.entity.*;
import org.example.multileanproject.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final QuizRepository quizRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AdminActionLogService adminActionLogService;

    private void assertLessonOwnership(Lesson lesson) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equals(a.getAuthority()));
        if (isAdmin) return;
        if (lesson.getSection() == null) return;
        if (lesson.getSection().getCourse() == null) return;
        if (lesson.getSection().getCourse().getInstructor() == null) return;
        if (lesson.getSection().getCourse().getInstructor().getUser() == null) return;
        String ownerEmail = lesson.getSection().getCourse().getInstructor().getUser().getEmail();
        if (!auth.getName().equalsIgnoreCase(ownerEmail)) {
            throw new RuntimeException("Bạn không có quyền thao tác với bài học này.");
        }
    }

    // ================= CREATE =================
    @Override
    public Lesson createLesson(Long sectionId, LessonRequest request) {

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        Lesson lesson = new Lesson();
        lesson.setTitle(request.getTitle());
        lesson.setType(request.getType());
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setSection(section);

        applyLessonType(lesson, request);

        return lessonRepository.save(lesson);
    }

    // ================= UPDATE =================
    @Override
    public Lesson updateLesson(Long lessonId, LessonRequest request) {

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        assertLessonOwnership(lesson);

        lesson.setTitle(request.getTitle());
        lesson.setType(request.getType());
        lesson.setOrderIndex(request.getOrderIndex());

        applyLessonType(lesson, request);

        return lessonRepository.save(lesson);
    }

    // ================= DELETE =================
    @Override
    public void deleteLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        assertLessonOwnership(lesson);

        Long courseId = lesson.getSection() != null && lesson.getSection().getCourse() != null
                ? lesson.getSection().getCourse().getId()
                : null;
        if (courseId != null && enrollmentRepository.countByCourse_Id(courseId) > 0) {
            adminActionLogService.log(
                    "BLOCK_HARD_DELETE_LESSON",
                    "Chan xoa lessonId=" + lessonId + " tren khoa hoc da co hoc vien",
                    courseId,
                    "COURSE"
            );
            throw new RuntimeException(
                    "Khoa hoc da co hoc vien. Khong duoc xoa lesson nay. Hay cap nhat noi dung thay vi xoa cung."
            );
        }

        lessonRepository.deleteById(lessonId);
    }

    // ================= GET =================
    @Override
    public List<Lesson> getLessonsBySection(Long sectionId) {
        return lessonRepository.findBySection_IdOrderByOrderIndexAsc(sectionId);
    }

    @Override
    public List<Lesson> getPreviewLessonsByCourse(Long courseId) {
        return lessonRepository.findPreviewLessonsByCourseId(courseId);
    }

    // ================= DURATION =================
    @Override
    public Integer getTotalDurationBySection(Long sectionId) {
        return lessonRepository.sumDurationBySectionId(sectionId);
    }

    @Override
    public Integer getTotalDurationByCourse(Long courseId) {
        return lessonRepository.sumDurationByCourseId(courseId);
    }

    // ================= HELPER =================
    private void applyLessonType(Lesson lesson, LessonRequest request) {

        // reset
        lesson.setVideoUrl(null);
        lesson.setDocumentUrl(null);
        lesson.setContentText(null);
        lesson.setDuration(0);
        lesson.setQuiz(null);

        switch (request.getType()) {
            case VIDEO -> {
                lesson.setVideoUrl(request.getVideoUrl());
                lesson.setDuration(request.getDuration());
            }
            case DOCUMENT -> {
                lesson.setDocumentUrl(request.getDocumentUrl());
            }
            case TEXT -> {
                lesson.setContentText(request.getContentText());
            }
            case QUIZ -> {
                if (request.getQuizId() == null) {
                    throw new RuntimeException("QuizId is required for QUIZ lesson");
                }
                Quiz quiz = quizRepository.findById(request.getQuizId())
                        .orElseThrow(() -> new RuntimeException("Quiz not found"));
                lesson.setQuiz(quiz);
            }
        }
    }
}
