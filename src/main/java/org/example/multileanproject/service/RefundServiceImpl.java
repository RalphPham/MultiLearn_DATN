package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.multileanproject.dto.RefundRequestDTO;
import org.example.multileanproject.entity.*;
import org.example.multileanproject.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundServiceImpl implements RefundService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final RefundRequestRepository refundRequestRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final OrderRepository orderRepository;
    private final CourseRepository courseRepository;
    private final NotificationService notificationService;
    private final CouponService couponService;

    @Override
    @Transactional
    public void createRefundRequest(String email, RefundRequestDTO dto) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học viên."));

        if (dto == null || dto.getCourseId() == null) {
            throw new RuntimeException("Thiếu courseId để gửi yêu cầu hoàn tiền.");
        }

        String reason = dto.getReason() != null ? dto.getReason().trim() : "";
        if (reason.isBlank()) {
            throw new RuntimeException("Vui lòng nhập lý do hoàn tiền.");
        }

        Enrollment enrollment = enrollmentRepository
                .findByStudent_IdAndCourse_Id(student.getId(), dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Bạn chưa đăng ký khoá học này."));

        if (!"ACTIVE".equalsIgnoreCase(enrollment.getStatus())) {
            throw new RuntimeException("Chỉ khoá học đang ACTIVE mới được gửi yêu cầu hoàn tiền.");
        }

        if (enrollment.getRefundLockedAt() != null) {
            throw new RuntimeException("Bạn đã bắt đầu học khoá học này nên không thể hoàn tiền.");
        }

        LocalDateTime enrolledAt = enrollment.getEnrolledAt();
        if (enrolledAt == null) {
            throw new RuntimeException("Không xác định được ngày mua khoá học.");
        }

        long daysSinceEnrollment = ChronoUnit.DAYS.between(enrolledAt, LocalDateTime.now());
        if (daysSinceEnrollment > 7) {
            throw new RuntimeException("Đã quá 7 ngày kể từ lúc mua khoá học.");
        }

        List<Order> completedOrders = orderRepository.findCompletedOrdersByStudentAndCourse(
                student.getId(),
                dto.getCourseId(),
                PageRequest.of(0, 5)
        );

        if (completedOrders.isEmpty()) {
            throw new RuntimeException("Không tìm thấy giao dịch COMPLETED cho khoá học này.");
        }

        Order order = completedOrders.get(0);

        // To keep schema compatibility (refund_requests only stores order_id),
        // we only allow refund requests for orders containing one course.
        long itemCount = refundRequestRepository.countOrderDetailsByOrderId(order.getId());
        if (itemCount != 1L) {
            throw new RuntimeException(
                    "Đơn hàng này chứa nhiều khoá học. Hiện tại chỉ hỗ trợ hoàn tiền đơn 1 khoá học."
            );
        }

        if (refundRequestRepository.existsByStudent_IdAndOrderId(student.getId(), order.getId())) {
            throw new RuntimeException("Bạn đã gửi yêu cầu hoàn tiền cho khoá học này.");
        }

        BigDecimal paidPrice = resolvePaidPrice(order, dto.getCourseId());

        if (paidPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Khoá học miễn phí không áp dụng hoàn tiền.");
        }

        BigDecimal refundAmount = paidPrice;

        RefundRequest request = RefundRequest.builder()
                .orderId(order.getId())
                .student(student)
                .amount(refundAmount)
                .reason(reason)
                .status(STATUS_PENDING)
                .build();

        refundRequestRepository.save(request);
        log.info(
                "Created refund request id={} studentId={} courseId={} orderId={} amount={}",
                request.getId(),
                student.getId(),
                dto.getCourseId(),
                order.getId(),
                refundAmount
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllRefundRequests() {
        List<Object[]> rows = refundRequestRepository.findAdminRefundRows();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row[0]);
            item.put("orderId", row[1]);
            item.put("amount", row[2]);
            item.put("reason", row[3]);
            item.put("status", row[4]);
            item.put("createdAt", row[5]);
            item.put("adminNote", row[6]);
            item.put("resolvedAt", row[7]);
            item.put("courseId", row[8]);
            item.put("courseName", row[9]);
            item.put("studentName", row[10]);
            item.put("studentEmail", row[11]);
            item.put("progress", row[12]);
            item.put("enrolledAt", row[13]);
            item.put("orderStatus", row[14]);
            item.put("paymentMethod", row[15]);
            item.put("transactionRef", row[16]);
            BigDecimal requestAmount = toBigDecimal(row[2]);
            BigDecimal progress = toBigDecimal(row[12]);
            BigDecimal originalAmount = toBigDecimal(row[17]);
            BigDecimal finalAmount = toBigDecimal(row[18]);
            if (finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                // Legacy fallback: suy ra finalAmount tu amount hoan theo tien do
                // amount = finalAmount * (100 - progress) / 100
                BigDecimal clampedProgress = progress.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));
                BigDecimal remainingPercent = BigDecimal.valueOf(100).subtract(clampedProgress);
                if (requestAmount.compareTo(BigDecimal.ZERO) > 0 && remainingPercent.compareTo(BigDecimal.ZERO) > 0) {
                    finalAmount = requestAmount
                            .multiply(BigDecimal.valueOf(100))
                            .divide(remainingPercent, 2, RoundingMode.HALF_UP);
                } else {
                    finalAmount = requestAmount;
                }
            }
            if (originalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                originalAmount = finalAmount;
            }
            BigDecimal discountAmount = originalAmount.subtract(finalAmount);
            if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
                discountAmount = BigDecimal.ZERO;
            }
            BigDecimal discountPercent = BigDecimal.ZERO;
            if (originalAmount.compareTo(BigDecimal.ZERO) > 0) {
                discountPercent = discountAmount
                        .multiply(BigDecimal.valueOf(100))
                        .divide(originalAmount, 2, RoundingMode.HALF_UP);
            }
            item.put("originalAmount", originalAmount);
            item.put("finalAmount", finalAmount);
            item.put("discountAmount", discountAmount);
            item.put("discountPercent", discountPercent);
            item.put("couponCode", row[19]);
            result.add(item);
        }

        return result;
    }

    @Override
    @Transactional
    public void approveRefund(Long refundId, String adminNote) {
        RefundRequest request = getPendingRefundOrThrow(refundId);

        Long courseId = refundRequestRepository.findFirstCourseIdByOrderId(request.getOrderId());
        if (courseId == null) {
            throw new RuntimeException("Không tìm thấy khoá học trong đơn hàng hoàn tiền.");
        }

        Enrollment enrollment = enrollmentRepository
                .findByStudent_IdAndCourse_Id(request.getStudent().getId(), courseId)
                .orElse(null);

        if (enrollment != null) {
            enrollment.setStatus("BLOCKED");
            enrollmentRepository.save(enrollment);
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng hoàn tiền."));
        order.setNote(appendRefundMarker(order.getNote(), request));
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);

        request.setStatus(STATUS_APPROVED);
        request.setAdminNote(cleanNote(adminNote));
        request.setResolvedAt(LocalDateTime.now());
        refundRequestRepository.save(request);

        Course course = courseRepository.findById(courseId).orElse(null);
        String courseTitle = course != null && course.getTitle() != null ? course.getTitle() : "khoa hoc";

        notificationService.createNotification(
                request.getStudent().getId(),
                "Hoàn tiền đã được duyệt",
                "Yêu cầu hoàn tiền cho khoá học '" + courseTitle + "' đã được phê duyệt.",
                "/my-courses",
                "STUDENT_REFUND_APPROVED",
                Notification.NotificationCategory.SYSTEM_NOTIFICATION,
                true,
                courseTitle,
                null
        );

    }

    @Override
    @Transactional
    public void rejectRefund(Long refundId, String adminNote) {
        RefundRequest request = getPendingRefundOrThrow(refundId);
        String cleanedNote = cleanNote(adminNote);

        if (cleanedNote == null || cleanedNote.isBlank()) {
            throw new RuntimeException("Vui lòng nhập lý do từ chối.");
        }

        Long courseId = refundRequestRepository.findFirstCourseIdByOrderId(request.getOrderId());
        Course course = courseId != null ? courseRepository.findById(courseId).orElse(null) : null;
        String courseTitle = course != null && course.getTitle() != null ? course.getTitle() : "khoa hoc";

        request.setStatus(STATUS_REJECTED);
        request.setAdminNote(cleanedNote);
        request.setResolvedAt(LocalDateTime.now());
        refundRequestRepository.save(request);

        notificationService.createNotification(
                request.getStudent().getId(),
                "Yêu cầu hoàn tiền bị từ chối",
                "Yêu cầu hoàn tiền cho khoá học '" + courseTitle + "' đã bị từ chối.",
                "/my-courses",
                "STUDENT_REFUND_REJECTED",
                Notification.NotificationCategory.SYSTEM_NOTIFICATION,
                true,
                courseTitle,
                null
        );

    }

    private RefundRequest getPendingRefundOrThrow(Long refundId) {
        RefundRequest request = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu hoàn tiền."));

        if (!STATUS_PENDING.equalsIgnoreCase(request.getStatus())) {
            throw new RuntimeException("Yêu cầu này đã được xử lý trước đó.");
        }
        return request;
    }

    private String cleanNote(String note) {
        if (note == null) {
            return null;
        }
        String trimmed = note.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String appendRefundMarker(String existingNote, RefundRequest request) {
        String marker = "[REFUND_APPROVED] request#" + request.getId() + " amount=" + request.getAmount();
        if (existingNote == null || existingNote.isBlank()) {
            return marker;
        }
        if (existingNote.contains("[REFUND_APPROVED]")) {
            return existingNote;
        }
        return existingNote + " | " + marker;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal resolvePaidPrice(Order order, Long courseId) {
        BigDecimal finalAmount = order.getFinalAmount() != null ? order.getFinalAmount() : BigDecimal.ZERO;
        BigDecimal originalAmount = order.getOriginalAmount() != null ? order.getOriginalAmount() : BigDecimal.ZERO;

        // Happy path: don hang co final_amount (so tien thuc tra).
        if (finalAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (originalAmount.compareTo(BigDecimal.ZERO) > 0 && finalAmount.compareTo(originalAmount) > 0) {
                return originalAmount;
            }
            return finalAmount;
        }

        // Legacy path: final_amount bi thieu, don co coupon -> thu tinh lai discount.
        String couponCode = order.getCouponCode() != null ? order.getCouponCode().trim() : "";
        if (!couponCode.isBlank()) {
            try {
                List<Long> instructorUserIds = order.getOrderItems().stream()
                        .map(OrderDetail::getCourse)
                        .filter(Objects::nonNull)
                        .map(Course::getInstructor)
                        .filter(Objects::nonNull)
                        .map(Instructor::getUser)
                        .filter(Objects::nonNull)
                        .map(Student::getId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());

                if (originalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal discount = couponService.calculateDiscount(couponCode, originalAmount, instructorUserIds);
                    BigDecimal recomputedFinal = originalAmount.subtract(discount);
                    if (recomputedFinal.compareTo(BigDecimal.ZERO) > 0) {
                        return recomputedFinal;
                    }
                }
            } catch (Exception ignored) {
                // Neu coupon da het han / thay doi, khong tinh lai duoc thi xu ly o duoi.
            }

            throw new RuntimeException(
                    "Đơn hàng có mã giảm giá nhưng thiếu số tiền thực trả. Vui lòng liên hệ admin để đối soát."
            );
        }

        // Khong co coupon: fallback theo gia line-item.
        return order.getOrderItems().stream()
                .filter(item -> item.getCourse() != null && Objects.equals(item.getCourse().getId(), courseId))
                .map(OrderDetail::getPrice)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giá khoá học trong đơn hàng."));
    }
}
