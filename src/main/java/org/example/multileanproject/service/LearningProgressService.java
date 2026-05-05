package org.example.multileanproject.service;

public interface LearningProgressService {
    double markLessonCompleted(String email, Long lessonId);
    String getLessonVideoUrl(String email, Long lessonId);
    String getLessonDocumentUrl(String email, Long lessonId);
    void markCourseStarted(String email, Long courseId);
}