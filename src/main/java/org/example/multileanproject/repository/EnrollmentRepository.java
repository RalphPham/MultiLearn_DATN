package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Enrollment;
import org.example.multileanproject.repository.projection.InstructorCourseEnrollmentView;
import org.example.multileanproject.repository.projection.InstructorStudentItemView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudent_IdAndCourse_Id(Long studentId, Long courseId);

    long countByCourse_Id(Long courseId);

    long countByCourse_IdAndStatusNot(Long courseId, String status);

    boolean existsByStudent_IdAndCourse_IdAndStatus(Long studentId, Long courseId, String status);

    boolean existsByStudent_EmailAndCourse_Id(String email, Long courseId);

    boolean existsByStudent_EmailAndCourse_IdAndStatus(String email, Long courseId, String status);

    List<Enrollment> findByStudent_Id(Long studentId);

    List<Enrollment> findByStudent_IdAndStatus(Long studentId, String status);

    Optional<Enrollment> findByStudent_IdAndCourse_Id(Long studentId, Long courseId);

    Optional<Enrollment> findByStudent_EmailAndCourse_Id(String email, Long courseId);

    List<Enrollment> findByStudent_IdAndCourse_IdIn(Long studentId, List<Long> courseIds);

    // ════════════════════════════════════════════════════════
    // ĐÃ SỬA: Đổi countByCreatedAtAfter thành countByEnrolledAtAfter
    // để khớp chính xác với tên biến 'enrolledAt' trong Enrollment.java
    // ════════════════════════════════════════════════════════
    long countByEnrolledAtAfter(LocalDateTime dateTime);

    @Query(value = """
        SELECT COUNT(DISTINCT e.student_id)
        FROM enrollments e
        INNER JOIN courses c ON e.course_id = c.id
        INNER JOIN instructors i ON c.instructor_id = i.id
        INNER JOIN students s ON i.user_id = s.id
        WHERE s.email = :email
          AND e.status = 'ACTIVE'
        """, nativeQuery = true)
    Long countDistinctStudentsByInstructorEmail(@Param("email") String email);

    @Query(value = """
        SELECT COUNT(e.id)
        FROM enrollments e
        INNER JOIN courses c ON e.course_id = c.id
        INNER JOIN instructors i ON c.instructor_id = i.id
        INNER JOIN students s ON i.user_id = s.id
        WHERE s.email = :email
          AND e.status = 'ACTIVE'
        """, nativeQuery = true)
    Long countActiveEnrollmentsByInstructorEmail(@Param("email") String email);

    @Query(value = """
        SELECT
            c.id AS courseId,
            c.title AS courseTitle,
            c.slug AS courseSlug,
            COUNT(e.id) AS activeEnrollments,
            COUNT(DISTINCT e.student_id) AS uniqueStudents
        FROM courses c
        INNER JOIN instructors i ON c.instructor_id = i.id
        INNER JOIN students s ON i.user_id = s.id
        LEFT JOIN enrollments e
               ON e.course_id = c.id
              AND e.status = 'ACTIVE'
        WHERE s.email = :email
        GROUP BY c.id, c.title, c.slug
        ORDER BY COUNT(DISTINCT e.student_id) DESC, c.id DESC
        """, nativeQuery = true)
    List<InstructorCourseEnrollmentView> findCourseEnrollmentStatsByInstructorEmail(@Param("email") String email);

    @Query(value = """
    SELECT
        e.id AS enrollmentId,

        s.id AS studentId,
        s.full_name AS studentName,
        s.email AS studentEmail,
        s.avatar AS studentAvatar,

        c.id AS courseId,
        c.title AS courseTitle,
        c.slug AS courseSlug,
        c.thumbnail AS courseThumbnail,

        e.status AS enrollmentStatus,
        e.progress AS progress,
        e.enrolled_at AS enrolledAt
    FROM enrollments e
    INNER JOIN courses c ON e.course_id = c.id
    INNER JOIN instructors i ON c.instructor_id = i.id
    INNER JOIN students owner_student ON i.user_id = owner_student.id
    INNER JOIN students s ON e.student_id = s.id
    WHERE owner_student.email = :email
      AND e.status = 'ACTIVE'
    ORDER BY c.id DESC, s.full_name ASC
    """, nativeQuery = true)
    List<InstructorStudentItemView> findStudentItemsByInstructorEmail(@Param("email") String email);

    @Query(value = """
    SELECT
        e.id AS enrollmentId,
        s.id AS studentId,
        s.full_name AS studentName,
        s.email AS studentEmail,
        s.avatar AS studentAvatar,
        c.id AS courseId,
        c.title AS courseTitle,
        c.slug AS courseSlug,
        c.thumbnail AS courseThumbnail,
        e.status AS enrollmentStatus,
        e.progress AS progress,
        e.enrolled_at AS enrolledAt
    FROM enrollments e
    INNER JOIN courses c ON e.course_id = c.id
    INNER JOIN instructors i ON c.instructor_id = i.id
    INNER JOIN students owner_student ON i.user_id = owner_student.id
    INNER JOIN students s ON e.student_id = s.id
    WHERE owner_student.email = :email
      AND e.status = 'ACTIVE'
      AND (e.progress IS NULL OR e.progress < 5)
      AND DATEDIFF(day, e.enrolled_at, GETDATE()) > 7
    ORDER BY e.enrolled_at ASC
    """, nativeQuery = true)
    List<InstructorStudentItemView> findAtRiskStudentsByInstructorEmail(@Param("email") String email);

    // Lấy danh sách Student đang học một khóa cụ thể
    @Query("SELECT e.student FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'ACTIVE'")
    List<org.example.multileanproject.entity.Student> findActiveStudentsByCourseId(@Param("courseId") Long courseId);

    /** Leaderboard: top 50 học viên theo tiến độ (completed first, then progress DESC) */
    @Query(value = """
        SELECT TOP 50
               e.id             AS enrollmentId,
               s.id             AS studentId,
               s.full_name      AS studentFullName,
               s.email          AS studentEmail,
               s.avatar         AS studentAvatar,
               e.progress       AS progress,
               e.is_course_completed  AS isCourseCompleted,
               e.course_completed_at  AS courseCompletedAt
        FROM enrollments e
        INNER JOIN students s ON e.student_id = s.id
        WHERE e.course_id = :courseId AND e.status = 'ACTIVE'
        ORDER BY CASE WHEN e.is_course_completed = 1 THEN 0 ELSE 1 END ASC,
                 e.progress DESC
        """, nativeQuery = true)
    List<Object[]> findLeaderboardNative(@Param("courseId") Long courseId);

    // ── T-11 Achievement queries ──────────────────────────────────────────────

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :studentId AND e.status = 'ACTIVE' AND e.isCourseCompleted = true")
    long countCompletedByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :studentId AND e.status = 'ACTIVE'")
    long countActiveByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(DISTINCT e.course.category.id) FROM Enrollment e WHERE e.student.id = :studentId AND e.status = 'ACTIVE' AND e.course.category IS NOT NULL")
    long countDistinctCategoriesByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT MIN(e.courseCompletedAt) FROM Enrollment e WHERE e.student.id = :studentId AND e.status = 'ACTIVE' AND e.isCourseCompleted = true")
    java.time.LocalDateTime findFirstCompletedAt(@Param("studentId") Long studentId);

    @Query(value = """
        SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END
        FROM enrollments e
        WHERE e.student_id = :studentId
          AND e.status = 'ACTIVE'
          AND e.is_course_completed = 1
          AND e.course_completed_at IS NOT NULL
          AND e.enrolled_at IS NOT NULL
          AND DATEDIFF(day, e.enrolled_at, e.course_completed_at) <= 7
        """, nativeQuery = true)
    int isFastLearner(@Param("studentId") Long studentId);

    // Lấy danh sách tài khoản User của các Giảng viên mà Học viên đang học
    @Query("SELECT DISTINCT s FROM Enrollment e " +
            "JOIN e.course c " +
            "JOIN c.instructor i " +
            "JOIN i.user s " +
            "WHERE e.student.id = :studentId AND e.status = 'ACTIVE'")
    List<org.example.multileanproject.entity.Student> findInstructorsByStudentId(@Param("studentId") Long studentId);

    // Lấy danh sách khóa học mà student đang học với một instructor cụ thể (tránh lazy load)
    @Query("SELECT c FROM Enrollment e JOIN e.course c JOIN c.instructor i JOIN i.user s " +
            "WHERE e.student.id = :studentId AND s.id = :instructorId AND e.status = 'ACTIVE'")
    List<org.example.multileanproject.entity.Course> findCoursesByStudentAndInstructor(
            @Param("studentId") Long studentId,
            @Param("instructorId") Long instructorId);
}