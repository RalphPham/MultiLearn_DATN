package org.example.multileanproject.repository;
import org.example.multileanproject.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho Entity Lesson
 * Cung cấp các phương thức truy vấn bài học
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findBySection_IdOrderByOrderIndexAsc(Long sectionId);

    Long countBySection_Id(Long sectionId);

    long countBySection_Course_Id(Long courseId);

    void deleteBySection_Id(Long sectionId);

    @Query("SELECT COALESCE(MAX(l.orderIndex), -1) FROM Lesson l " +
            "WHERE l.section.id = :sectionId")
    Integer findMaxOrderIndexBySectionId(@Param("sectionId") Long sectionId);

    @Query("SELECT l FROM Lesson l " +
            "WHERE l.section.course.id = :courseId " +
            "AND l.isPreview = true " +
            "ORDER BY l.section.orderIndex, l.orderIndex")
    List<Lesson> findPreviewLessonsByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COALESCE(SUM(l.duration), 0) FROM Lesson l " +
            "WHERE l.section.course.id = :courseId")
    Integer sumDurationByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COALESCE(SUM(l.duration), 0) FROM Lesson l " +
            "WHERE l.section.id = :sectionId")
    Integer sumDurationBySectionId(@Param("sectionId") Long sectionId);

    @Query("SELECT l FROM Lesson l JOIN FETCH l.section s JOIN FETCH s.course c " +
            "WHERE c.instructor.user.id = :instructorUserId " +
            "ORDER BY c.title ASC, s.orderIndex ASC, l.orderIndex ASC")
    List<Lesson> findAllByInstructorUserId(@Param("instructorUserId") Long instructorUserId);
}
