package org.example.multileanproject.service;

import org.example.multileanproject.dto.LessonRequest;
import org.example.multileanproject.entity.Lesson;

import java.util.List;

public interface LessonService {

    Lesson createLesson(Long sectionId, LessonRequest request);

    Lesson updateLesson(Long lessonId, LessonRequest request);

    void deleteLesson(Long lessonId);

    List<Lesson> getLessonsBySection(Long sectionId);

    List<Lesson> getPreviewLessonsByCourse(Long courseId);

    Integer getTotalDurationBySection(Long sectionId);

    Integer getTotalDurationByCourse(Long courseId);
}
