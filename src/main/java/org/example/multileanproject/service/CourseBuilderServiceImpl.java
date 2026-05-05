package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.CourseContentRequest;
import org.example.multileanproject.dto.LessonRequest;
import org.example.multileanproject.dto.SectionRequest;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.Lesson;
import org.example.multileanproject.entity.Quiz;
import org.example.multileanproject.entity.Section;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.LessonRepository;
import org.example.multileanproject.repository.QuizRepository;
import org.example.multileanproject.repository.SectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseBuilderServiceImpl implements CourseBuilderService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;

    @Override
    @Transactional
    public void updateFullContent(Long courseId, CourseContentRequest request) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));

        // 1️⃣ BẮT BUỘC CÓ ÍT NHẤT 1 CHƯƠNG
        if (request.getSections() == null || request.getSections().isEmpty()) {
            throw new RuntimeException("Khóa học phải có ít nhất 1 chương");
        }

        // 2️⃣ XÓA TOÀN BỘ NỘI DUNG CŨ
        sectionRepository.deleteAllByCourseId(courseId);

        // 3️⃣ THÊM LẠI CHƯƠNG & BÀI HỌC
        int sectionIndex = 1;
        for (SectionRequest sectionReq : request.getSections()) {

            if (sectionReq.getLessons() == null || sectionReq.getLessons().isEmpty()) {
                throw new RuntimeException("Mỗi chương phải có ít nhất 1 bài học");
            }

            Section section = new Section();
            section.setTitle(sectionReq.getTitle());
            section.setOrderIndex(sectionIndex++);
            section.setDescription(sectionReq.getDescription());
            section.setCourse(course);

            sectionRepository.save(section);

            int lessonIndex = 1;
            for (LessonRequest lessonReq : sectionReq.getLessons()) {

                validateLesson(lessonReq);

                Lesson lesson = new Lesson();
                lesson.setTitle(lessonReq.getTitle());
                lesson.setType(lessonReq.getType());
                lesson.setOrderIndex(lessonIndex++);
                lesson.setSection(section);

                // XỬ LÝ THEO LOẠI BÀI HỌC
                switch (lessonReq.getType()) {
                    case VIDEO -> {
                        lesson.setVideoUrl(lessonReq.getVideoUrl());
                        lesson.setDuration(lessonReq.getDuration());
                    }
                    case DOCUMENT -> lesson.setDocumentUrl(lessonReq.getDocumentUrl());
                    case TEXT -> lesson.setContentText(lessonReq.getContentText());
                    case QUIZ -> {
                        Quiz quiz = quizRepository.findById(lessonReq.getQuizId())
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài kiểm tra"));
                        lesson.setQuiz(quiz);
                    }
                }

                lessonRepository.save(lesson);
            }
        }
    }

    // ================= VALIDATE =================
    private void validateLesson(LessonRequest req) {

        if (req.getType() == null) {
            throw new RuntimeException("Loại bài học không được để trống");
        }

        switch (req.getType()) {
            case VIDEO -> {
                if (req.getVideoUrl() == null || req.getDuration() == null) {
                    throw new RuntimeException("Bài học VIDEO cần có link video và thời lượng");
                }
            }
            case QUIZ -> {
                if (req.getQuizId() == null) {
                    throw new RuntimeException("Bài học QUIZ cần có quizId");
                }
            }
        }
    }
}
