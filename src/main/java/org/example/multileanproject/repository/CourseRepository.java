package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.example.multileanproject.dto.TopCourseRevenueDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // 🔥 ĐÃ SỬA: Xóa bỏ hoàn toàn ORDER BY CASE để không bị lỗi với SQL Server
    @Query(
            value = """
                    SELECT c
                    FROM Course c
                    LEFT JOIN FETCH c.category
                    LEFT JOIN FETCH c.instructor i
                    LEFT JOIN FETCH i.user u
                    WHERE c.status = org.example.multileanproject.entity.CourseStatus.PUBLISHED
                      AND (:keyword IS NULL OR :keyword = ''
                           OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(c.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          )
                      AND (:minPrice IS NULL OR (CASE WHEN c.salePrice IS NOT NULL AND c.salePrice > 0 THEN c.salePrice ELSE c.price END) >= :minPrice)
                      AND (:maxPrice IS NULL OR (CASE WHEN c.salePrice IS NOT NULL AND c.salePrice > 0 THEN c.salePrice ELSE c.price END) <= :maxPrice)
                      AND (:categoryId IS NULL OR c.category.id = :categoryId OR c.category.parent.id = :categoryId)
                    """,
            countQuery = """
                    SELECT COUNT(c)
                    FROM Course c
                    LEFT JOIN c.instructor i
                    LEFT JOIN i.user u
                    WHERE c.status = org.example.multileanproject.entity.CourseStatus.PUBLISHED
                      AND (:keyword IS NULL OR :keyword = ''
                           OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(c.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          )
                      AND (:minPrice IS NULL OR (CASE WHEN c.salePrice IS NOT NULL AND c.salePrice > 0 THEN c.salePrice ELSE c.price END) >= :minPrice)
                      AND (:maxPrice IS NULL OR (CASE WHEN c.salePrice IS NOT NULL AND c.salePrice > 0 THEN c.salePrice ELSE c.price END) <= :maxPrice)
                      AND (:categoryId IS NULL OR c.category.id = :categoryId OR c.category.parent.id = :categoryId)
                    """
    )
    Page<Course> searchCoursesPublic(
            @Param("keyword") String keyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    /** Top N published courses by studentCount — dùng cho Blog trending prompt */
    @Query("""
            SELECT c FROM Course c
            LEFT JOIN FETCH c.category
            LEFT JOIN FETCH c.instructor i
            LEFT JOIN FETCH i.user
            WHERE c.status = org.example.multileanproject.entity.CourseStatus.PUBLISHED
            ORDER BY c.studentCount DESC
            """)
    List<Course> findTopPublishedByStudentCount(Pageable pageable);

    /** One published course with full joins — dùng cho Blog spotlight prompt */
    @Query("""
            SELECT c FROM Course c
            LEFT JOIN FETCH c.category
            LEFT JOIN FETCH c.instructor i
            LEFT JOIN FETCH i.user
            WHERE c.id = :id
            """)
    java.util.Optional<Course> findByIdWithJoins(@Param("id") Long id);

    /**
     * Dùng khóa ngoại thật courses.instructor_id
     * Đây là method cần dùng cho instructor dashboard.
     */
    @Query("""
            SELECT DISTINCT c
            FROM Course c
            LEFT JOIN FETCH c.category
            LEFT JOIN FETCH c.instructor i
            LEFT JOIN FETCH i.user
            WHERE i.id = :instructorId
            ORDER BY c.createdAt DESC
            """)
    List<Course> findByInstructorIdWithDetails(@Param("instructorId") Long instructorId);

    /**
     * Giữ lại để không làm gãy chỗ khác nếu còn dùng.
     * Nhưng instructor dashboard sẽ KHÔNG dùng method này nữa.
     */
    @Query("""
            SELECT DISTINCT c
            FROM Course c
            LEFT JOIN FETCH c.category
            LEFT JOIN FETCH c.instructor i
            LEFT JOIN FETCH i.user
            WHERE LOWER(i.user.email) = LOWER(:email)
            ORDER BY c.createdAt DESC
            """)
    List<Course> findByInstructorEmail(@Param("email") String email);

    @Query("""
            SELECT DISTINCT c
            FROM Course c
            LEFT JOIN FETCH c.sections s
            LEFT JOIN FETCH c.category
            LEFT JOIN FETCH c.instructor i
            LEFT JOIN FETCH i.user
            WHERE c.slug = :slug
            """)
    Optional<Course> findBySlugWithDetails(@Param("slug") String slug);

    @Query("""
            SELECT DISTINCT c
            FROM Course c
            LEFT JOIN FETCH c.sections s
            LEFT JOIN FETCH c.category
            LEFT JOIN FETCH c.instructor i
            LEFT JOIN FETCH i.user
            WHERE c.id = :id
            """)
    Optional<Course> findByIdWithDetails(@Param("id") Long id);

    Optional<Course> findBySlug(String slug);

    @Query(
            value = """
                    SELECT DISTINCT c
                    FROM Course c
                    LEFT JOIN FETCH c.category
                    LEFT JOIN FETCH c.instructor i
                    LEFT JOIN FETCH i.user
                    WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
                      AND (:categoryId IS NULL OR c.category.id = :categoryId)
                      AND (:status IS NULL OR c.status = :status)
                    """,
            countQuery = """
                    SELECT COUNT(c)
                    FROM Course c
                    WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
                      AND (:categoryId IS NULL OR c.category.id = :categoryId)
                      AND (:status IS NULL OR c.status = :status)
                    """
    )
    Page<Course> searchCoursesForAdmin(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") CourseStatus status,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT DISTINCT c
                    FROM Course c
                    LEFT JOIN FETCH c.category
                    LEFT JOIN FETCH c.instructor i
                    LEFT JOIN FETCH i.user
                    WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
                      AND (:categoryId IS NULL OR c.category.id = :categoryId)
                      AND c.status IN :statuses
                    """,
            countQuery = """
                    SELECT COUNT(c)
                    FROM Course c
                    WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
                      AND (:categoryId IS NULL OR c.category.id = :categoryId)
                      AND c.status IN :statuses
                    """
    )
    Page<Course> searchCoursesForAdminByStatuses(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("statuses") List<CourseStatus> statuses,
            Pageable pageable
    );

    @Query("SELECT COUNT(c) FROM Course c WHERE c.status = org.example.multileanproject.entity.CourseStatus.PENDING_APPROVAL")
    long countPendingCourses();

    @Query("SELECT COUNT(c) FROM Course c WHERE c.status = :status")
    long countByStatus(@Param("status") CourseStatus status);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.status = :status AND c.createdAt BETWEEN :from AND :to")
    long countByStatusAndDateRange(@Param("status") CourseStatus status,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);

    @Query(value = """
        SELECT YEAR(COALESCE(c.updated_at, c.created_at)) AS yyyy,
               MONTH(COALESCE(c.updated_at, c.created_at)) AS mm,
               COUNT(*) AS total
        FROM courses c
        WHERE c.status = 'PUBLISHED'
          AND (:start IS NULL OR COALESCE(c.updated_at, c.created_at) >= :start)
          AND (:end IS NULL OR COALESCE(c.updated_at, c.created_at) <= :end)
        GROUP BY YEAR(COALESCE(c.updated_at, c.created_at)),
                 MONTH(COALESCE(c.updated_at, c.created_at))
        ORDER BY YEAR(COALESCE(c.updated_at, c.created_at)),
                 MONTH(COALESCE(c.updated_at, c.created_at))
        """, nativeQuery = true)
    List<Object[]> countPublishedCoursesByMonth(
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end
    );

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"category", "instructor", "instructor.user"})
    List<Course> findByStatus(CourseStatus status);
    @Query("""
        SELECT new org.example.multileanproject.dto.TopCourseRevenueDTO(
            c.id,
            c.title,
            c.thumbnail,
            COALESCE(COUNT(CASE
                WHEN o.status = 'COMPLETED' THEN od.id
                ELSE null
            END), 0),
            COALESCE(SUM(CASE
                WHEN o.status = 'COMPLETED' THEN od.price * COALESCE(c.commissionRate, 0)
                ELSE 0
            END), 0)
        )
        FROM Course c
        LEFT JOIN OrderDetail od ON od.course.id = c.id
        LEFT JOIN od.order o
        WHERE c.instructor.user.email = :instructorEmail
          AND (o.id IS NULL OR :fromDate IS NULL OR o.createdAt >= :fromDate)
          AND (o.id IS NULL OR :toDate   IS NULL OR o.createdAt <  :toDate)
        GROUP BY c.id, c.title, c.thumbnail
        ORDER BY COALESCE(SUM(CASE
            WHEN o.status = 'COMPLETED' THEN od.price * COALESCE(c.commissionRate, 0)
            ELSE 0
        END), 0) DESC
    """)
    List<TopCourseRevenueDTO> getTopRevenueCourses(@Param("instructorEmail") String instructorEmail,
                                                   @Param("fromDate") LocalDateTime fromDate,
                                                   @Param("toDate")   LocalDateTime toDate);
    @Query("SELECT c FROM Course c WHERE c.instructor.user.id = :userId")
    List<Course> findByInstructorUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.instructor.user.id = :userId")
    long countByInstructorUserId(@Param("userId") Long userId);

    /** T-10: Gợi ý khóa học dựa trên category student đang học, chưa enrolled, top 8 */
    @Query(value = """
        SELECT TOP 8
               c.id, c.title, c.slug, c.thumbnail,
               c.price, c.sale_price,
               c.level, c.average_rating, c.student_count,
               c.total_lessons, c.total_duration,
               cat.name   AS categoryName,
               s.full_name AS instructorName,
               s.avatar    AS instructorAvatar
        FROM courses c
        INNER JOIN categories cat ON c.category_id = cat.id
        INNER JOIN instructors i  ON c.instructor_id = i.id
        INNER JOIN students s     ON i.user_id = s.id
        WHERE c.status = 'PUBLISHED'
          AND c.category_id IN (
              SELECT DISTINCT c2.category_id
              FROM enrollments e2
              INNER JOIN courses  c2  ON e2.course_id  = c2.id
              INNER JOIN students st2 ON e2.student_id = st2.id
              WHERE st2.email = :email AND e2.status = 'ACTIVE'
                AND c2.category_id IS NOT NULL
          )
          AND c.id NOT IN (
              SELECT e3.course_id
              FROM enrollments e3
              INNER JOIN students st3 ON e3.student_id = st3.id
              WHERE st3.email = :email
          )
        ORDER BY c.average_rating DESC, c.student_count DESC
        """, nativeQuery = true)
    List<Object[]> findRecommendedForStudent(@Param("email") String email);

    /** T-10 (AI): top 20 ứng viên phổ biến chưa enrolled — để Gemini chọn từ danh sách rộng hơn */
    @Query(value = """
        SELECT TOP 20
               c.id, c.title, c.slug, c.thumbnail,
               c.price, c.sale_price,
               c.level, c.average_rating, c.student_count,
               c.total_lessons, c.total_duration,
               cat.name   AS categoryName,
               s.full_name AS instructorName,
               s.avatar    AS instructorAvatar
        FROM courses c
        INNER JOIN categories cat ON c.category_id = cat.id
        INNER JOIN instructors i  ON c.instructor_id = i.id
        INNER JOIN students s     ON i.user_id = s.id
        WHERE c.status = 'PUBLISHED'
          AND c.id NOT IN (
              SELECT e.course_id
              FROM enrollments e
              INNER JOIN students st ON e.student_id = st.id
              WHERE st.email = :email
          )
        ORDER BY c.student_count DESC, c.average_rating DESC
        """, nativeQuery = true)
    List<Object[]> findTop20PopularExcludingEnrolled(@Param("email") String email);

    /** T-10: Fallback — top 8 khóa phổ biến nhất (dùng khi student chưa enrolled course nào có category) */
    @Query(value = """
        SELECT TOP 8
               c.id, c.title, c.slug, c.thumbnail,
               c.price, c.sale_price,
               c.level, c.average_rating, c.student_count,
               c.total_lessons, c.total_duration,
               cat.name   AS categoryName,
               s.full_name AS instructorName,
               s.avatar    AS instructorAvatar
        FROM courses c
        INNER JOIN categories cat ON c.category_id = cat.id
        INNER JOIN instructors i  ON c.instructor_id = i.id
        INNER JOIN students s     ON i.user_id = s.id
        WHERE c.status = 'PUBLISHED'
          AND c.id NOT IN (
              SELECT e.course_id
              FROM enrollments e
              INNER JOIN students st ON e.student_id = st.id
              WHERE st.email = :email
          )
        ORDER BY c.student_count DESC, c.average_rating DESC
        """, nativeQuery = true)
    List<Object[]> findPopularExcludingEnrolled(@Param("email") String email);
}
