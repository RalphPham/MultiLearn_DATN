package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);
    Optional<Student> findByEmailIgnoreCase(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    // ── Tìm kiếm ──────────────────────────────────────────────────────────

    @Query(value = """
        SELECT * FROM students s
        WHERE (:keyword IS NULL OR :keyword = ''
               OR s.full_name COLLATE Latin1_General_CI_AI LIKE '%' + :keyword + '%'
               OR s.email     COLLATE Latin1_General_CI_AI LIKE '%' + :keyword + '%')
          AND (:role IS NULL OR :role = '' OR s.role = :role)
          AND (:activeFilter IS NULL OR s.is_active = :activeFilter)
        """,
            countQuery = """
        SELECT COUNT(*) FROM students s
        WHERE (:keyword IS NULL OR :keyword = ''
               OR s.full_name COLLATE Latin1_General_CI_AI LIKE '%' + :keyword + '%'
               OR s.email     COLLATE Latin1_General_CI_AI LIKE '%' + :keyword + '%')
          AND (:role IS NULL OR :role = '' OR s.role = :role)
          AND (:activeFilter IS NULL OR s.is_active = :activeFilter)
        """,
            nativeQuery = true)
    Page<Student> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") String role,
            @Param("activeFilter") Boolean activeFilter,
            Pageable pageable
    );

    // ── Đếm cho cột "Khóa học" ────────────────────────────────────────────

    /** STUDENT: số khóa đang học */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :studentId")
    int countEnrollments(@Param("studentId") Long studentId);

    /**
     * INSTRUCTOR: số khóa sở hữu.
     * 🔥 SỬA: i.student → i.user  (field trong Instructor.java là "private Student user")
     */
    @Query("""
        SELECT COUNT(c)
        FROM Course c
        JOIN c.instructor i
        WHERE i.user.id = :studentId
        """)
    int countOwnedCourses(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.student.id = :userId AND o.status = 'COMPLETED'")
    int countPurchasedCourses(@Param("userId") Long userId);

    // ── Admin Dashboard ────────────────────────────────────────────────────

    /** Đếm học viên mới (role=STUDENT) theo từng tháng từ ngày :since */
    @Query("""
        SELECT YEAR(s.createdAt), MONTH(s.createdAt), COUNT(s)
        FROM Student s
        WHERE s.role = org.example.multileanproject.entity.Role.STUDENT
          AND s.createdAt >= :since
        GROUP BY YEAR(s.createdAt), MONTH(s.createdAt)
        ORDER BY YEAR(s.createdAt), MONTH(s.createdAt)
        """)
    List<Object[]> countNewStudentsByMonth(@Param("since") LocalDateTime since);

    @Query("""
        SELECT YEAR(s.createdAt), MONTH(s.createdAt), COUNT(s)
        FROM Student s
        WHERE s.role = org.example.multileanproject.entity.Role.STUDENT
          AND s.createdAt >= :since
          AND s.createdAt <= :until
        GROUP BY YEAR(s.createdAt), MONTH(s.createdAt)
        ORDER BY YEAR(s.createdAt), MONTH(s.createdAt)
        """)
    List<Object[]> countNewStudentsByMonthBetween(@Param("since") LocalDateTime since,
                                                  @Param("until") LocalDateTime until);

    /** Tổng học viên mới trong khoảng thời gian */
    @Query("""
        SELECT COUNT(s) FROM Student s
        WHERE s.role = org.example.multileanproject.entity.Role.STUDENT
          AND s.createdAt >= :since
        """)
    long countNewStudentsSince(@Param("since") LocalDateTime since);

    @Query("""
        SELECT COUNT(s) FROM Student s
        WHERE s.role = org.example.multileanproject.entity.Role.STUDENT
          AND s.createdAt BETWEEN :from AND :to
        """)
    long countStudentsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // ── Danh sách chi tiết (Drawer tab) ───────────────────────────────────

    /**
     * STUDENT — khóa đang học + % tiến độ.
     * lp.isCompleted (field Java) = cột is_completed trong DB.
     */
    @Query("""
        SELECT
            c.id,
            c.title,
            c.slug,
            c.thumbnail,
            c.status,
            c.price,
            c.salePrice,
            e.enrolledAt,
            cat.name,
            (SELECT COUNT(lp)
             FROM LearningProgress lp
             WHERE lp.student.id = :studentId
               AND lp.lesson.section.course.id = c.id
               AND lp.isCompleted = true) * 100
            / NULLIF(
                (SELECT COUNT(l2) FROM Lesson l2 WHERE l2.section.course.id = c.id),
              0)
        FROM Enrollment e
        JOIN e.course c
        LEFT JOIN c.category cat
        WHERE e.student.id = :studentId
        ORDER BY e.enrolledAt DESC
        """)
    List<Object[]> findEnrolledCoursesByStudentId(@Param("studentId") Long studentId);

    /**
     * INSTRUCTOR — khóa sở hữu + số học viên.
     * 🔥 SỬA: i.student → i.user
     */
    @Query("""
        SELECT
            c.id,
            c.title,
            c.slug,
            c.thumbnail,
            c.status,
            c.price,
            c.salePrice,
            c.createdAt,
            cat.name,
            (SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = c.id)
        FROM Course c
        JOIN c.instructor i
        LEFT JOIN c.category cat
        WHERE i.user.id = :instructorStudentId
        ORDER BY c.createdAt DESC
        """)
    List<Object[]> findCoursesByInstructorStudentId(@Param("instructorStudentId") Long instructorStudentId);
}
