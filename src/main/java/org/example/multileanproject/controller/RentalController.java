package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.entity.*;
import org.example.multileanproject.repository.*;
import org.example.multileanproject.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RentalController {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final OrderRepository orderRepository;

    /**
     * POST /api/student/courses/{id}/rent
     * Tạo Order rental → trả về { orderId, finalAmount } để frontend redirect VNPay.
     * Nếu finalAmount = 0 (khóa miễn phí) thì kích hoạt luôn.
     */
    @PostMapping("/api/student/courses/{id}/rent")
    public ResponseEntity<?> rent(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body,
            Authentication authentication) {

        if (authentication == null) return ResponseEntity.status(401).build();
        String email = authentication.getName();

        int duration = body.getOrDefault("duration", 30);
        if (duration != 7 && duration != 30 && duration != 90) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thời hạn thuê không hợp lệ (7/30/90 ngày)."));
        }

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học."));

        if (!Boolean.TRUE.equals(course.getRentalEnabled())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Khóa học này không hỗ trợ thuê."));
        }

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học viên."));

        // Kiểm tra đã mua vĩnh viễn chưa
        Enrollment existing = enrollmentRepository.findByStudent_IdAndCourse_Id(student.getId(), id).orElse(null);
        if (existing != null && "ACTIVE".equals(existing.getStatus()) && existing.getRentalExpiresAt() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bạn đã sở hữu vĩnh viễn khóa học này."));
        }

        BigDecimal rentalPrice = calcRentalPrice(course.getPrice(), duration);

        // Tạo Order rental
        Order order = Order.builder()
                .student(student)
                .originalAmount(rentalPrice)
                .finalAmount(rentalPrice)
                .paymentMethod("VNPAY")
                .note("RENTAL:" + duration)
                .status(OrderStatus.PENDING)
                .build();

        OrderDetail detail = OrderDetail.builder()
                .order(order)
                .course(course)
                .price(rentalPrice)
                .build();

        order.setOrderItems(List.of(detail));
        Order saved = orderRepository.save(order);

        // Nếu miễn phí → kích hoạt luôn
        if (rentalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            saved.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(saved);
            enrollmentService.activateRental(student.getId(), id, duration);
            return ResponseEntity.ok(Map.of(
                    "message", "Thuê thành công!",
                    "orderId", saved.getId(),
                    "finalAmount", 0,
                    "activated", true
            ));
        }

        return ResponseEntity.ok(Map.of(
                "orderId", saved.getId(),
                "finalAmount", rentalPrice,
                "activated", false
        ));
    }

    /**
     * POST /api/student/courses/{id}/renew — gia hạn thuê (qua VNPay)
     */
    @PostMapping("/api/student/courses/{id}/renew")
    public ResponseEntity<?> renew(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body,
            Authentication authentication) {

        if (authentication == null) return ResponseEntity.status(401).build();
        String email = authentication.getName();

        int duration = body.getOrDefault("duration", 30);
        if (duration != 7 && duration != 30 && duration != 90) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thời hạn thuê không hợp lệ (7/30/90 ngày)."));
        }

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học."));

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học viên."));

        BigDecimal rentalPrice = calcRentalPrice(course.getPrice(), duration);

        Order order = Order.builder()
                .student(student)
                .originalAmount(rentalPrice)
                .finalAmount(rentalPrice)
                .paymentMethod("VNPAY")
                .note("RENTAL:" + duration)
                .status(OrderStatus.PENDING)
                .build();

        OrderDetail detail = OrderDetail.builder()
                .order(order).course(course).price(rentalPrice).build();

        order.setOrderItems(List.of(detail));
        Order saved = orderRepository.save(order);

        return ResponseEntity.ok(Map.of(
                "orderId", saved.getId(),
                "finalAmount", rentalPrice,
                "activated", false
        ));
    }

    /** PUT /api/admin/courses/{id}/rental — Admin bật/tắt cho thuê */
    @PutMapping("/api/admin/courses/{id}/rental")
    public ResponseEntity<?> toggleRental(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body,
            Authentication authentication) {

        if (authentication == null) return ResponseEntity.status(401).build();

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học."));

        boolean enabled = Boolean.TRUE.equals(body.get("enabled"));
        course.setRentalEnabled(enabled);
        courseRepository.save(course);

        return ResponseEntity.ok(Map.of(
                "rentalEnabled", enabled,
                "message", enabled ? "Đã bật tính năng thuê khóa học." : "Đã tắt tính năng thuê khóa học."
        ));
    }

    private BigDecimal calcRentalPrice(BigDecimal coursePrice, int duration) {
        if (coursePrice == null || coursePrice.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        double pct = duration == 7 ? 0.15 : duration == 30 ? 0.30 : 0.50;
        return coursePrice.multiply(BigDecimal.valueOf(pct)).setScale(0, RoundingMode.HALF_UP);
    }

    // Inject EnrollmentService để kích hoạt miễn phí
    private final EnrollmentService enrollmentService;
}
