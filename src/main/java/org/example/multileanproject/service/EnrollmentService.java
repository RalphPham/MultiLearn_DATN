package org.example.multileanproject.service;

import org.example.multileanproject.dto.MyCourseResponse;
import java.util.List;

public interface EnrollmentService {

    // Kích hoạt khóa học (Dùng khi thanh toán thành công)
    void activateCourse(Long studentId, Long courseId);

    // Lấy danh sách khóa học của tôi
    List<MyCourseResponse> getMyCourses(String email);

    // ✅ THÊM HÀM NÀY ĐỂ FIX LỖI CONTROLLER
    boolean isStudentEnrolled(Long studentId, Long courseId);

    /** Kích hoạt khóa học thuê (có thời hạn) */
    void activateRental(Long studentId, Long courseId, int durationDays);
}