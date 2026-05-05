package org.example.multileanproject.repository;

import org.example.multileanproject.entity.WithdrawRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawRequestRepository extends JpaRepository<WithdrawRequest, Long> {

    List<WithdrawRequest> findByInstructor_IdOrderByCreatedAtDesc(Long instructorId);

    boolean existsByInstructor_IdAndStatus(Long instructorId, String status);

    @Query("""
        SELECT w
        FROM WithdrawRequest w
        WHERE (:status IS NULL OR w.status = :status)
        ORDER BY w.createdAt DESC
    """)
    Page<WithdrawRequest> findAllByStatusFilter(@Param("status") String status, Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(w.amount), 0) FROM WithdrawRequest w WHERE w.instructor.id = :instructorId AND w.status = 'APPROVED'")
    java.math.BigDecimal sumApprovedWithdrawsByInstructorId(@Param("instructorId") Long instructorId);
}

