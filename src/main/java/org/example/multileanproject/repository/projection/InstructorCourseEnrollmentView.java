package org.example.multileanproject.repository.projection;

public interface InstructorCourseEnrollmentView {
    Long getCourseId();
    String getCourseTitle();
    String getCourseSlug();
    Long getActiveEnrollments();
    Long getUniqueStudents();
}