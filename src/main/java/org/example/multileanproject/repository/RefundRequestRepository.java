package org.example.multileanproject.repository;

import org.example.multileanproject.entity.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

    boolean existsByStudent_IdAndOrderId(Long studentId, Long orderId);

    @Query(value = """
        WITH latest_refund AS (
            SELECT
                od.course_id AS courseId,
                rr.status AS status,
                ROW_NUMBER() OVER (
                    PARTITION BY od.course_id
                    ORDER BY rr.created_at DESC, rr.id DESC
                ) AS rn
            FROM refund_requests rr
            JOIN order_details od ON od.order_id = rr.order_id
            WHERE rr.student_id = :studentId
              AND od.course_id IN (:courseIds)
        )
        SELECT courseId, status
        FROM latest_refund
        WHERE rn = 1
        """, nativeQuery = true)
    List<Object[]> findLatestStatusByStudentAndCourseIds(
            @Param("studentId") Long studentId,
            @Param("courseIds") List<Long> courseIds
    );

    @Query(value = """
        SELECT
            rr.id,
            rr.order_id AS orderId,
            rr.amount,
            rr.reason,
            rr.status,
            rr.created_at AS createdAt,
            rr.admin_note AS adminNote,
            rr.resolved_at AS resolvedAt,
            od.course_id AS courseId,
            c.title AS courseName,
            s.full_name AS studentName,
            s.email AS studentEmail,
            e.progress,
            e.enrolled_at AS enrolledAt,
            o.status AS orderStatus,
            o.payment_method AS paymentMethod,
            o.transaction_ref AS transactionRef,
            o.original_amount AS originalAmount,
            o.final_amount AS finalAmount,
            o.coupon_code AS couponCode
        FROM refund_requests rr
        JOIN students s ON s.id = rr.student_id
        JOIN orders o ON o.id = rr.order_id
        OUTER APPLY (
            SELECT TOP 1 od1.course_id
            FROM order_details od1
            WHERE od1.order_id = rr.order_id
            ORDER BY od1.id ASC
        ) od
        LEFT JOIN courses c ON c.id = od.course_id
        LEFT JOIN enrollments e ON e.student_id = rr.student_id AND e.course_id = od.course_id
        ORDER BY rr.created_at DESC, rr.id DESC
        """, nativeQuery = true)
    List<Object[]> findAdminRefundRows();

    @Query(value = """
        SELECT COUNT(*)
        FROM refund_requests rr
        JOIN order_details od ON od.order_id = rr.order_id
        WHERE rr.student_id = :studentId
          AND od.course_id = :courseId
          AND rr.status = 'APPROVED'
        """, nativeQuery = true)
    long countApprovedRefundByStudentAndCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId
    );

    @Query(value = """
        SELECT COUNT(*)
        FROM refund_requests rr
        JOIN order_details od ON od.order_id = rr.order_id
        WHERE rr.student_id = :studentId
          AND od.course_id = :courseId
          AND rr.status = 'PENDING'
        """, nativeQuery = true)
    long countPendingRefundByStudentAndCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId
    );

    @Query(value = "SELECT COUNT(*) FROM order_details WHERE order_id = :orderId", nativeQuery = true)
    long countOrderDetailsByOrderId(@Param("orderId") Long orderId);

    @Query(value = """
        SELECT TOP 1 course_id
        FROM order_details
        WHERE order_id = :orderId
        ORDER BY id ASC
        """, nativeQuery = true)
    Long findFirstCourseIdByOrderId(@Param("orderId") Long orderId);

    @Query("""
        SELECT rr.orderId
        FROM RefundRequest rr
        WHERE rr.status = 'APPROVED'
          AND rr.orderId IN :orderIds
        """)
    Set<Long> findApprovedRefundOrderIds(@Param("orderIds") List<Long> orderIds);

    Optional<RefundRequest> findTopByOrderIdAndStatusOrderByResolvedAtDescIdDesc(Long orderId, String status);
}
