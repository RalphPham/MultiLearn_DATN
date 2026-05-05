package org.example.multileanproject.repository;

import org.example.multileanproject.entity.CourseChangeRequest;
import org.example.multileanproject.entity.CourseChangeRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseChangeRequestRepository extends JpaRepository<CourseChangeRequest, Long> {

    // Tìm request PENDING hiện tại của 1 course (dùng khi auto-cancel và kiểm tra trùng)
    Optional<CourseChangeRequest> findFirstByCourse_IdAndStatusOrderByCreatedAtDesc(
            Long courseId, CourseChangeRequestStatus status);

    // Tất cả PENDING của 1 course (dùng khi auto-cancel hàng loạt)
    List<CourseChangeRequest> findAllByCourse_IdAndStatus(
            Long courseId, CourseChangeRequestStatus status);

    // Instructor xem request mới nhất của course (bất kể status)
    Optional<CourseChangeRequest> findFirstByCourse_IdAndInstructor_IdOrderByCreatedAtDesc(
            Long courseId, Long instructorId);

    // Admin xem danh sách theo status + type, mới nhất trước
    Page<CourseChangeRequest> findByStatusAndRequestTypeOrderByCreatedAtDesc(
            CourseChangeRequestStatus status, String requestType, Pageable pageable);
}
