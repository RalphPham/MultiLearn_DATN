package org.example.multileanproject.service;

import org.example.multileanproject.dto.CourseDetailDTO;
import org.example.multileanproject.dto.CourseListDTO;
import org.example.multileanproject.dto.CourseRequestDTO;
import org.example.multileanproject.dto.InstructorCourseListItemDTO;
import org.example.multileanproject.entity.Course;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface CourseService {
    List<InstructorCourseListItemDTO> getMyCourses(String email);

    Course createCourseWithInstructor(Course course, String email);

    Course updateCourse(Long courseId, CourseRequestDTO request);

    void deleteCourse(Long courseId);

    void submitForApproval(Long courseId);

    Course createCourse(Course course);

    CourseDetailDTO getLearningCourseDetail(Long courseId, String email);

    List<Course> getAllCourses();

    Page<CourseListDTO> searchCoursesPublic(Map<String, String> params, int page, int size);

    CourseDetailDTO getCourseDetailBySlug(String slug, String currentEmail);

    CourseDetailDTO getCourseDetail(Long courseId, String currentEmail);

    void toggleSaleStatus(Long courseId, String email);
}