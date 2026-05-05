package org.example.multileanproject.repository.projection;

public interface InstructorStudentItemView {
    Long getEnrollmentId();

    Long getStudentId();
    String getStudentName();
    String getStudentEmail();
    String getStudentAvatar();

    Long getCourseId();
    String getCourseTitle();
    String getCourseSlug();
    String getCourseThumbnail();

    String getEnrollmentStatus();
    Double getProgress();
    java.time.LocalDateTime getEnrolledAt();
}