package org.example.multileanproject.repository;

import org.example.multileanproject.entity.CourseApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CourseApprovalHistoryRepository extends JpaRepository<CourseApprovalHistory, Long> {
    List<CourseApprovalHistory> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    Optional<CourseApprovalHistory> findTopByCourseIdOrderByCreatedAtDescIdDesc(Long courseId);
}
