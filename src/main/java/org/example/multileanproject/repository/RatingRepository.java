package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByCourse_IdOrderByCreatedAtDesc(Long courseId);

    boolean existsByCourse_IdAndStudent_Id(Long courseId, Long studentId);

    Optional<Rating> findByCourse_IdAndStudent_Id(Long courseId, Long studentId);

    long countByCourse_Id(Long courseId);

    List<Rating> findByCourse_Instructor_IdOrderByCreatedAtDesc(Long instructorId);

    @Query("SELECT COALESCE(AVG(r.stars), 0) FROM Rating r WHERE r.course.id = :courseId")
    Double getAverageRatingByCourseId(@Param("courseId") Long courseId);

    @Query("""
            SELECT r
            FROM Rating r
            WHERE r.course.instructor.user.email = :email
            ORDER BY r.createdAt DESC
            """)
    List<Rating> findRatingsByInstructorEmail(@Param("email") String email);

    /** Xu hướng đánh giá theo tháng (năm, tháng, avg, count) */
    @Query("""
            SELECT YEAR(r.createdAt), MONTH(r.createdAt),
                   AVG(r.stars), COUNT(r)
            FROM Rating r
            WHERE r.course.instructor.user.email = :email
            GROUP BY YEAR(r.createdAt), MONTH(r.createdAt)
            ORDER BY YEAR(r.createdAt) ASC, MONTH(r.createdAt) ASC
            """)
    List<Object[]> findMonthlyTrendByInstructorEmail(@Param("email") String email);

    /** Phân phối số sao (1-5) */
    @Query("""
            SELECT r.stars, COUNT(r)
            FROM Rating r
            WHERE r.course.instructor.user.email = :email
            GROUP BY r.stars
            ORDER BY r.stars ASC
            """)
    List<Object[]> findStarDistributionByInstructorEmail(@Param("email") String email);
}