package org.example.multileanproject.repository;

import org.example.multileanproject.entity.LearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {

    @Query("SELECT COUNT(lp) FROM LearningProgress lp " +
            "WHERE lp.student.id = :studentId " +
            "AND lp.lesson.section.course.id = :courseId " +
            "AND lp.isCompleted = true")
    long countCompletedLessons(@Param("studentId") Long studentId,
                               @Param("courseId") Long courseId);

    boolean existsByStudent_IdAndLesson_Id(Long studentId, Long lessonId);

    Optional<LearningProgress> findByStudent_IdAndLesson_Id(Long studentId, Long lessonId);

    @Query("SELECT lp.lesson.id FROM LearningProgress lp " +
            "WHERE lp.student.id = :studentId " +
            "AND lp.lesson.section.course.id = :courseId " +
            "AND lp.isCompleted = true")
    List<Long> findCompletedLessonIds(@Param("studentId") Long studentId,
                                      @Param("courseId") Long courseId);

    /** T-11: Lấy danh sách ngày học (có completed lesson) của student, sắp xếp DESC */
    @Query(value = """
        SELECT DISTINCT CAST(lp.completed_at AS DATE) AS day
        FROM learning_progress lp
        INNER JOIN students s ON lp.student_id = s.id
        WHERE s.email = :email
          AND lp.is_completed = 1
          AND lp.completed_at IS NOT NULL
        ORDER BY day DESC
        """, nativeQuery = true)
    List<Date> findDistinctLearnDates(@Param("email") String email);

    @Query("SELECT l.id, l.title, sec.title, l.orderIndex, sec.orderIndex, COUNT(lp.id) " +
            "FROM Lesson l " +
            "JOIN l.section sec " +
            "LEFT JOIN LearningProgress lp ON lp.lesson.id = l.id AND lp.isCompleted = true " +
            "WHERE sec.course.id = :courseId " +
            "GROUP BY l.id, l.title, sec.title, l.orderIndex, sec.orderIndex " +
            "ORDER BY sec.orderIndex, l.orderIndex")
    List<Object[]> getLessonCompletionsByCourse(@Param("courseId") Long courseId);
}