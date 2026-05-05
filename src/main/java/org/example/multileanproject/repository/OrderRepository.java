package org.example.multileanproject.repository;

import org.example.multileanproject.dto.RecentTransactionDTO;
import org.example.multileanproject.entity.Order;
import java.time.LocalDateTime;
import org.example.multileanproject.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.orderItems oi " +
            "WHERE o.student.id = :studentId " +
            "AND oi.course.id = :courseId " +
            "AND o.status = :status")
    boolean existsByStudentIdAndCourseIdAndStatus(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("status") OrderStatus status
    );

    @Query("SELECT o FROM Order o JOIN o.student s WHERE " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Order> searchOrders(@Param("keyword") String keyword, @Param("status") OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o JOIN o.student s WHERE " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY o.id DESC")
    List<Order> searchOrdersAll(@Param("keyword") String keyword, @Param("status") OrderStatus status);

    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.status = :status")
    BigDecimal sumRevenueByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :from AND :to")
    BigDecimal sumRevenueByStatusAndRange(@Param("status") OrderStatus status,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :from AND :to")
    long countByStatusAndRange(@Param("status") OrderStatus status,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to);

    List<Order> findByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);

    // Native SQL — SQL Server không cho phép NOT EXISTS bên trong SUM/COUNT → dùng LEFT JOIN.
    @Query(value = """
        SELECT
            COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' AND ar.order_id IS NULL
                              THEN od.price ELSE 0 END), 0),
            COALESCE(SUM(CASE WHEN ar.order_id IS NOT NULL
                              THEN od.price ELSE 0 END), 0),
            COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' AND ar.order_id IS NULL
                              THEN od.price * COALESCE(c.commission_rate, 0) ELSE 0 END), 0),
            COALESCE(COUNT(DISTINCT CASE WHEN o.status = 'COMPLETED' AND ar.order_id IS NULL
                                         THEN o.student_id ELSE NULL END), 0),
            COALESCE(COUNT(CASE WHEN o.status = 'COMPLETED' AND ar.order_id IS NULL
                                THEN od.id ELSE NULL END), 0)
        FROM order_details od
        JOIN orders o ON o.id = od.order_id
        JOIN courses c ON c.id = od.course_id
        JOIN instructors i ON i.id = c.instructor_id
        JOIN students u ON u.id = i.user_id
        LEFT JOIN (
            SELECT DISTINCT order_id FROM refund_requests WHERE status = 'APPROVED'
        ) ar ON ar.order_id = o.id
        WHERE u.email = :instructorEmail
          AND (:fromDate IS NULL OR o.created_at >= :fromDate)
          AND (:toDate   IS NULL OR o.created_at <  :toDate)
    """, nativeQuery = true)
    List<Object[]> calculateRevenueMetrics(@Param("instructorEmail") String instructorEmail,
                                           @Param("fromDate") LocalDateTime fromDate,
                                           @Param("toDate")   LocalDateTime toDate);

    @Query(value = """
        SELECT
            YEAR(o.created_at),
            MONTH(o.created_at),
            COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' AND ar.order_id IS NULL
                              THEN od.price ELSE 0 END), 0),
            COALESCE(SUM(CASE WHEN ar.order_id IS NOT NULL
                              THEN od.price ELSE 0 END), 0),
            COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' AND ar.order_id IS NULL
                              THEN od.price * COALESCE(c.commission_rate, 0) ELSE 0 END), 0)
        FROM order_details od
        JOIN orders o ON o.id = od.order_id
        JOIN courses c ON c.id = od.course_id
        JOIN instructors i ON i.id = c.instructor_id
        JOIN students u ON u.id = i.user_id
        LEFT JOIN (
            SELECT DISTINCT order_id FROM refund_requests WHERE status = 'APPROVED'
        ) ar ON ar.order_id = o.id
        WHERE u.email = :instructorEmail
          AND (:fromDate IS NULL OR o.created_at >= :fromDate)
          AND (:toDate   IS NULL OR o.created_at <  :toDate)
        GROUP BY YEAR(o.created_at), MONTH(o.created_at)
        ORDER BY YEAR(o.created_at), MONTH(o.created_at)
    """, nativeQuery = true)
    List<Object[]> getRevenueChart(@Param("instructorEmail") String instructorEmail,
                                   @Param("fromDate") LocalDateTime fromDate,
                                   @Param("toDate")   LocalDateTime toDate);

    @Query("""
        SELECT new org.example.multileanproject.dto.RecentTransactionDTO(
            o.student.fullName,
            c.title,
            od.price,
            o.createdAt
        )
        FROM OrderDetail od
        JOIN od.order o
        JOIN od.course c
        WHERE c.instructor.user.email = :instructorEmail
          AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
          AND (:toDate   IS NULL OR o.createdAt <  :toDate)
          AND o.status IN ('COMPLETED', 'REFUNDED')
        ORDER BY o.createdAt DESC
    """)
    List<RecentTransactionDTO> getRecentTransactions(@Param("instructorEmail") String instructorEmail,
                                                     @Param("fromDate") LocalDateTime fromDate,
                                                     @Param("toDate")   LocalDateTime toDate);

    @Query("""
        SELECT od.course.id, od.course.title, od.course.thumbnail,
               COUNT(od.id), SUM(od.price)
        FROM OrderDetail od
        JOIN od.order o
        WHERE o.status = org.example.multileanproject.entity.OrderStatus.COMPLETED
        GROUP BY od.course.id, od.course.title, od.course.thumbnail
        ORDER BY SUM(od.price) DESC
        """)
    List<Object[]> findTopCoursesByRevenue(Pageable pageable);

    @Query("""
        SELECT od.course.id, od.course.title, od.course.thumbnail,
               COUNT(od.id), SUM(od.price)
        FROM OrderDetail od
        JOIN od.order o
        WHERE o.status = org.example.multileanproject.entity.OrderStatus.COMPLETED
          AND (:from IS NULL OR o.createdAt >= :from)
          AND (:to   IS NULL OR o.createdAt <= :to)
        GROUP BY od.course.id, od.course.title, od.course.thumbnail
        ORDER BY SUM(od.price) DESC
        """)
    List<Object[]> findTopCoursesByRevenueInRange(@Param("from") LocalDateTime from,
                                                  @Param("to")   LocalDateTime to,
                                                  Pageable pageable);

    @Query("""
        SELECT od.course.instructor.user.id,
               od.course.instructor.user.fullName,
               od.course.instructor.user.avatar,
               COUNT(DISTINCT od.course.id),
               SUM(od.price)
        FROM OrderDetail od
        JOIN od.order o
        WHERE o.status = org.example.multileanproject.entity.OrderStatus.COMPLETED
        GROUP BY od.course.instructor.user.id,
                 od.course.instructor.user.fullName,
                 od.course.instructor.user.avatar
        ORDER BY SUM(od.price) DESC
        """)
    List<Object[]> findTopInstructorsByRevenue(Pageable pageable);

    @Query("""
        SELECT od.course.instructor.user.id,
               od.course.instructor.user.fullName,
               od.course.instructor.user.avatar,
               COUNT(DISTINCT od.course.id),
               SUM(od.price)
        FROM OrderDetail od
        JOIN od.order o
        WHERE o.status = org.example.multileanproject.entity.OrderStatus.COMPLETED
          AND (:from IS NULL OR o.createdAt >= :from)
          AND (:to   IS NULL OR o.createdAt <= :to)
        GROUP BY od.course.instructor.user.id,
                 od.course.instructor.user.fullName,
                 od.course.instructor.user.avatar
        ORDER BY SUM(od.price) DESC
        """)
    List<Object[]> findTopInstructorsByRevenueInRange(@Param("from") LocalDateTime from,
                                                      @Param("to")   LocalDateTime to,
                                                      Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(od.price), 0)
        FROM OrderDetail od
        JOIN od.order o
        WHERE o.status = org.example.multileanproject.entity.OrderStatus.COMPLETED
          AND od.course.instructor.user.id = :instructorUserId
        """)
    BigDecimal sumRevenueByInstructorUserId(@Param("instructorUserId") Long instructorUserId);

    @Query("""
        SELECT o.status, COUNT(o)
        FROM Order o
        GROUP BY o.status
        """)
    List<Object[]> countOrdersByStatus();

    @Query("""
        SELECT DISTINCT o
        FROM Order o
        JOIN o.orderItems od
        WHERE o.student.id = :studentId
          AND od.course.id = :courseId
          AND o.status = org.example.multileanproject.entity.OrderStatus.COMPLETED
        ORDER BY o.createdAt DESC
        """)
    List<Order> findCompletedOrdersByStudentAndCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            Pageable pageable
    );

    @Query(value = """
        WITH latest_paid AS (
            SELECT
                od.course_id AS courseId,
                o.original_amount AS originalAmount,
                o.final_amount AS finalAmount,
                o.coupon_code AS couponCode,
                ROW_NUMBER() OVER (
                    PARTITION BY od.course_id
                    ORDER BY o.created_at DESC, o.id DESC
                ) AS rn
            FROM orders o
            JOIN order_details od ON od.order_id = o.id
            WHERE o.student_id = :studentId
              AND o.status = 'COMPLETED'
              AND od.course_id IN (:courseIds)
        )
        SELECT courseId, originalAmount, finalAmount, couponCode
        FROM latest_paid
        WHERE rn = 1
        """, nativeQuery = true)
    List<Object[]> findLatestCompletedOrderSummaryByStudentAndCourseIds(
            @Param("studentId") Long studentId,
            @Param("courseIds") List<Long> courseIds
    );

    @Query(value = """
        SELECT CAST(o.created_at AS DATE) AS day, COALESCE(SUM(od.price), 0) AS revenue
        FROM order_details od
        JOIN orders o ON od.order_id = o.id
        JOIN courses c ON od.course_id = c.id
        JOIN instructors i ON c.instructor_id = i.id
        WHERE o.status = 'COMPLETED'
          AND i.user_id = :instructorUserId
          AND o.created_at BETWEEN :start AND :end
        GROUP BY CAST(o.created_at AS DATE)
        ORDER BY CAST(o.created_at AS DATE)
        """, nativeQuery = true)
    List<Object[]> findDailyRevenueByInstructor(
            @Param("instructorUserId") Long instructorUserId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = """
        SELECT YEAR(o.created_at) AS yyyy,
               MONTH(o.created_at) AS mm,
               COALESCE(SUM(o.final_amount), 0) AS revenue
        FROM orders o
        WHERE o.status = 'COMPLETED'
          AND (:start IS NULL OR o.created_at >= :start)
          AND (:end IS NULL OR o.created_at <= :end)
        GROUP BY YEAR(o.created_at), MONTH(o.created_at)
        ORDER BY YEAR(o.created_at), MONTH(o.created_at)
        """, nativeQuery = true)
    List<Object[]> findMonthlyRevenueAllPlatform(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Doanh thu theo tháng kèm phân chia platform / instructor dựa trên commission_rate
     * của từng course. Phân bổ final_amount của đơn theo tỉ lệ od.price / item_sum.
     * Trả: yyyy, mm, total, instructor_share, platform_share.
     */
    @Query(value = """
        SELECT YEAR(o.created_at) AS yyyy,
               MONTH(o.created_at) AS mm,
               COALESCE(SUM(
                 (od.price / NULLIF(ot.item_sum, 0))
                 * COALESCE(NULLIF(o.final_amount, 0), ot.item_sum)
               ), 0) AS total_revenue,
               COALESCE(SUM(
                 (od.price / NULLIF(ot.item_sum, 0))
                 * COALESCE(NULLIF(o.final_amount, 0), ot.item_sum)
                 * COALESCE(c.commission_rate, 0)
               ), 0) AS instructor_share,
               COALESCE(SUM(
                 (od.price / NULLIF(ot.item_sum, 0))
                 * COALESCE(NULLIF(o.final_amount, 0), ot.item_sum)
                 * (1 - COALESCE(c.commission_rate, 0))
               ), 0) AS platform_share
        FROM order_details od
        JOIN orders o ON o.id = od.order_id
        JOIN courses c ON c.id = od.course_id
        JOIN (SELECT order_id, SUM(price) AS item_sum FROM order_details GROUP BY order_id) ot
          ON ot.order_id = od.order_id
        WHERE o.status = 'COMPLETED'
          AND (:start IS NULL OR o.created_at >= :start)
          AND (:end IS NULL OR o.created_at <= :end)
        GROUP BY YEAR(o.created_at), MONTH(o.created_at)
        ORDER BY YEAR(o.created_at), MONTH(o.created_at)
    """, nativeQuery = true)
    List<Object[]> findMonthlyRevenueBreakdown(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Tổng thu nhập thực (net) của giảng viên:
     * - Chỉ đơn COMPLETED, chưa bị refund (APPROVED hoặc PENDING), đã qua 14 ngày kể từ ngày tạo đơn.
     * - Dùng final_amount (số tiền thực thu) phân bổ theo tỉ lệ giá item trong đơn.
     */
    @Query(value = """
        SELECT COALESCE(SUM(
            CASE
                WHEN o.status = 'COMPLETED'
                     AND ar.order_id IS NULL
                     AND pr.order_id IS NULL
                     AND o.created_at <= DATEADD(DAY, -14, GETDATE())
                THEN (od.price / NULLIF(ot.item_sum, 0))
                     * COALESCE(NULLIF(o.final_amount, 0), ot.item_sum)
                     * COALESCE(c.commission_rate, 0)
                ELSE 0
            END
        ), 0)
        FROM order_details od
        JOIN orders o ON o.id = od.order_id
        JOIN courses c ON c.id = od.course_id
        JOIN instructors i ON i.id = c.instructor_id
        JOIN students u ON u.id = i.user_id
        JOIN (
            SELECT order_id, SUM(price) AS item_sum
            FROM order_details
            GROUP BY order_id
        ) ot ON ot.order_id = od.order_id
        LEFT JOIN (
            SELECT DISTINCT order_id FROM refund_requests WHERE status = 'APPROVED'
        ) ar ON ar.order_id = o.id
        LEFT JOIN (
            SELECT DISTINCT order_id FROM refund_requests WHERE status = 'PENDING'
        ) pr ON pr.order_id = o.id
        WHERE u.email = :instructorEmail
    """, nativeQuery = true)
    java.math.BigDecimal calculateTotalNetEarnings(@Param("instructorEmail") String instructorEmail);

    @Query(value = """
        SELECT COALESCE(SUM(
            CASE WHEN o.status = 'COMPLETED' AND ar.order_id IS NULL
                 THEN (od.price / NULLIF(ot.item_sum, 0))
                      * COALESCE(NULLIF(o.final_amount, 0), ot.item_sum)
                      * COALESCE(c.commission_rate, 0)
                 ELSE 0 END
        ), 0)
        FROM order_details od
        JOIN orders o ON o.id = od.order_id
        JOIN courses c ON c.id = od.course_id
        JOIN (SELECT order_id, SUM(price) AS item_sum FROM order_details GROUP BY order_id) ot
          ON ot.order_id = od.order_id
        LEFT JOIN (SELECT DISTINCT order_id FROM refund_requests WHERE status = 'APPROVED') ar
          ON ar.order_id = o.id
        WHERE o.created_at BETWEEN :from AND :to
    """, nativeQuery = true)
    java.math.BigDecimal calculateTotalInstructorPayoutInRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /** Tổng hoa hồng đã chi cho tất cả giảng viên (đơn COMPLETED chưa bị APPROVED refund). */
    @Query(value = """
        SELECT COALESCE(SUM(
            CASE WHEN o.status = 'COMPLETED' AND ar.order_id IS NULL
                 THEN (od.price / NULLIF(ot.item_sum, 0))
                      * COALESCE(NULLIF(o.final_amount, 0), ot.item_sum)
                      * COALESCE(c.commission_rate, 0)
                 ELSE 0 END
        ), 0)
        FROM order_details od
        JOIN orders o ON o.id = od.order_id
        JOIN courses c ON c.id = od.course_id
        JOIN (SELECT order_id, SUM(price) AS item_sum FROM order_details GROUP BY order_id) ot
          ON ot.order_id = od.order_id
        LEFT JOIN (SELECT DISTINCT order_id FROM refund_requests WHERE status = 'APPROVED') ar
          ON ar.order_id = o.id
    """, nativeQuery = true)
    java.math.BigDecimal calculateTotalInstructorPayout();

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN o.orderItems item " +
            "WHERE o.student.id = :studentId " +
            "AND o.status = 'PENDING' " +
            "AND o.id <> :excludeOrderId " +
            "AND item.course.id IN :courseIds")
    List<Order> findDuplicatePendingOrders(
            @Param("studentId") Long studentId,
            @Param("excludeOrderId") Long excludeOrderId,
            @Param("courseIds") List<Long> courseIds
    );
}
