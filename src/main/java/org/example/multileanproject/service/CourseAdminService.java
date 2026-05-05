package org.example.multileanproject.service;

import org.example.multileanproject.dto.CourseApproveRequest;
import org.example.multileanproject.dto.CourseBulkApprovalRequest;
import org.example.multileanproject.dto.CourseDTO;
import org.example.multileanproject.entity.Category;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.CourseApprovalHistory;
import org.example.multileanproject.entity.CourseStatus;
import org.example.multileanproject.entity.Notification;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.entity.CourseChangeRequestStatus;
import org.example.multileanproject.repository.AdminRepository;
import org.example.multileanproject.repository.CategoryRepository;
import org.example.multileanproject.repository.CourseApprovalHistoryRepository;
import org.example.multileanproject.repository.CourseChangeRequestRepository;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.EnrollmentRepository;
import org.example.multileanproject.repository.SaleCampaignItemRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseAdminService {

    private static final double INSTRUCTOR_COMMISSION_RATE = 0.30d;
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationService notificationService;
    private final StudentRepository studentRepository;
    private final CourseApprovalHistoryRepository approvalHistoryRepository;
    private final AdminRepository adminRepository;
    private final AdminActionLogService adminActionLogService;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseChangeRequestRepository courseChangeRequestRepository;
    private final SaleCampaignItemRepository saleCampaignItemRepository;

    public CourseAdminService(CourseRepository courseRepository,
                              CategoryRepository categoryRepository,
                              NotificationService notificationService,
                              StudentRepository studentRepository,
                              CourseApprovalHistoryRepository approvalHistoryRepository,
                              AdminRepository adminRepository,
                              AdminActionLogService adminActionLogService,
                              EnrollmentRepository enrollmentRepository,
                              CourseChangeRequestRepository courseChangeRequestRepository,
                              SaleCampaignItemRepository saleCampaignItemRepository) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.notificationService = notificationService;
        this.studentRepository = studentRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
        this.adminRepository = adminRepository;
        this.adminActionLogService = adminActionLogService;
        this.enrollmentRepository = enrollmentRepository;
        this.courseChangeRequestRepository = courseChangeRequestRepository;
        this.saleCampaignItemRepository = saleCampaignItemRepository;
    }

    private Long getCurrentAdminId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return adminRepository.findByUsername(username)
                .map(a -> a.getId())
                .orElse(1L);
    }

    public Page<CourseDTO> getAllCourses(String keyword, Long categoryId, String statusStr, Pageable pageable) {
        CourseStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                if ("PENDING".equalsIgnoreCase(statusStr)) {
                    status = CourseStatus.PENDING_APPROVAL;
                } else {
                    status = CourseStatus.valueOf(statusStr);
                }
            } catch (Exception ignored) {
                status = null;
            }
        }

        if (status == CourseStatus.PENDING_APPROVAL) {
            return courseRepository.searchCoursesForAdminByStatuses(
                            keyword,
                            categoryId,
                            List.of(CourseStatus.PENDING_APPROVAL, CourseStatus.INACTIVE_REQUESTED),
                            pageable
                    )
                    .map(this::convertToDTO);
        }

        return courseRepository.searchCoursesForAdmin(keyword, categoryId, status, pageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public void approveCourseWithPrice(Long courseId, CourseApproveRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        String oldStatus = course.getStatus() != null ? course.getStatus().name() : "DRAFT";
        boolean isPauseRequest = course.getStatus() == CourseStatus.INACTIVE_REQUESTED;

        if (!isPauseRequest && request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Gia niem yet khong hop le.");
        }
        if (!isPauseRequest
                && request.getSalePrice() != null
                && request.getPrice() != null
                && request.getSalePrice().compareTo(request.getPrice()) >= 0) {
            throw new RuntimeException("Gia khuyen mai phai nho hon gia niem yet.");
        }
        // Khoá giá niêm yết nếu khoá học đã có học viên cũ (trường hợp duyệt lại sau khi giảng viên
        // cập nhật nội dung lớn). salePrice vẫn cho phép để admin có thể chạy khuyến mãi.
        boolean hasExistingStudents = course.getStudentCount() != null && course.getStudentCount() > 0;
        if (!isPauseRequest && hasExistingStudents
                && request.getPrice() != null
                && (course.getPrice() == null || request.getPrice().compareTo(course.getPrice()) != 0)) {
            throw new RuntimeException(
                    "Khóa học đã có học viên đăng ký nên không thể đổi giá niêm yết khi duyệt lại. "
                            + "Update nội dung sau khi bán là bonus miễn phí cho học viên.");
        }

        if (!isPauseRequest && request.getPrice() != null && !hasExistingStudents) {
            course.setPrice(request.getPrice());
        }
        if (!isPauseRequest && request.getSalePrice() != null) {
            course.setSalePrice(request.getSalePrice());
        }
        if (!isPauseRequest) {
            course.setCommissionRate(INSTRUCTOR_COMMISSION_RATE);
        }

        course.setStatus(isPauseRequest ? CourseStatus.INACTIVE : CourseStatus.PUBLISHED);
        courseRepository.save(course);

        if (isPauseRequest) {
            saveApprovalHistory(courseId, getCurrentAdminId(), oldStatus, "INACTIVE", "Admin duyet yeu cau tam dung ban.");
            sendNotificationToInstructor(course, "INACTIVE", "Yeu cau tam dung ban da duoc phe duyet.");
            adminActionLogService.log(
                    "APPROVE_PAUSE_SALE",
                    "Duyet yeu cau tam dung ban khoa hoc \"" + course.getTitle() + "\"",
                    courseId,
                    "COURSE"
            );
        } else {
            saveApprovalHistory(courseId, getCurrentAdminId(), oldStatus, "PUBLISHED", "Khoa hoc da duoc duyet va dinh gia.");
            sendNotificationToInstructor(course, "PUBLISHED", null);
            adminActionLogService.log(
                    "APPROVE_COURSE",
                    "Duyet khoa hoc \"" + course.getTitle() + "\"",
                    courseId,
                    "COURSE"
            );
        }
    }

    @Transactional
    public void updateCourseStatus(Long courseId, String newStatus, String reason) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        CourseStatus currentStatus = course.getStatus() != null ? course.getStatus() : CourseStatus.DRAFT;
        String oldStatus = currentStatus.name();

        CourseStatus requestedStatus;
        try {
            if ("PENDING".equalsIgnoreCase(newStatus)) {
                requestedStatus = CourseStatus.PENDING_APPROVAL;
            } else {
                requestedStatus = CourseStatus.valueOf(newStatus);
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid status: " + newStatus);
        }

        boolean rejectPauseRequest = currentStatus == CourseStatus.INACTIVE_REQUESTED
                && requestedStatus == CourseStatus.REJECTED;
        CourseStatus statusAfter = rejectPauseRequest ? CourseStatus.PUBLISHED : requestedStatus;

        course.setStatus(statusAfter);
        courseRepository.save(course);

        // Auto-cancel các PENDING change request khi course về trạng thái không phải PUBLISHED
        if (statusAfter == CourseStatus.REJECTED || statusAfter == CourseStatus.DRAFT
                || statusAfter == CourseStatus.BLOCKED) {
            autoCancelPendingChangeRequests(courseId,
                    "Tự động hủy do khóa học chuyển sang trạng thái " + statusAfter.name() + ".");
        }

        saveApprovalHistory(courseId, getCurrentAdminId(), oldStatus, statusAfter.name(), reason);

        if (rejectPauseRequest) {
            sendNotificationToInstructor(course, "PAUSE_REJECTED", reason);
        } else {
            sendNotificationToInstructor(course, statusAfter.name(), reason);
        }

        if (statusAfter == CourseStatus.BLOCKED) {
            sendNotificationToEnrolledStudents(course, reason);
        }

        String action;
        if (rejectPauseRequest) {
            action = "REJECT_PAUSE_SALE";
        } else if (requestedStatus == CourseStatus.REJECTED) {
            action = "REJECT_COURSE";
        } else {
            action = "UPDATE_COURSE_STATUS";
        }

        adminActionLogService.log(
                action,
                "Cap nhat trang thai khoa hoc \"" + course.getTitle() + "\" -> " + statusAfter.name()
                        + (reason != null && !reason.isBlank() ? " | Ly do: " + reason : ""),
                courseId,
                "COURSE"
        );
    }

    private void saveApprovalHistory(Long courseId, Long adminId, String statusBefore, String statusAfter, String reason) {
        CourseApprovalHistory history = CourseApprovalHistory.builder()
                .courseId(courseId)
                .adminId(adminId)
                .statusBefore(statusBefore)
                .statusAfter(statusAfter)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();
        approvalHistoryRepository.save(history);
    }

    private void sendNotificationToInstructor(Course course, String newStatus, String reason) {
        if (course.getInstructor() == null || course.getInstructor().getUser() == null) {
            return;
        }

        Student instructorAccount = course.getInstructor().getUser();
        String title = "Cap nhat trang thai khoa hoc";
        String message = "Khoa hoc da thay doi trang thai.";
        String type = "INSTRUCTOR_COURSE_STATUS_UPDATED";

        if ("PUBLISHED".equals(newStatus)) {
            title = "Khoa hoc da duoc duyet";
            message = "Khoa hoc \"" + course.getTitle() + "\" da duoc admin duyet va dinh gia.";
            type = "INSTRUCTOR_COURSE_APPROVED";
        } else if ("INACTIVE".equals(newStatus)) {
            title = "Yeu cau tam dung ban da duoc duyet";
            message = "Khoa hoc \"" + course.getTitle() + "\" da duoc admin phe duyet tam dung ban.";
            type = "INSTRUCTOR_COURSE_PAUSE_APPROVED";
        } else if ("PAUSE_REJECTED".equals(newStatus)) {
            title = "Yeu cau tam dung ban bi tu choi";
            message = "Admin khong phe duyet yeu cau tam dung ban khoa hoc \"" + course.getTitle() + "\"."
                    + (reason != null && !reason.isBlank() ? " Ly do: " + reason : "");
            type = "INSTRUCTOR_COURSE_PAUSE_REJECTED";
        } else if ("REJECTED".equals(newStatus)) {
            title = "Khoa hoc bi tu choi";
            message = "Khoa hoc \"" + course.getTitle() + "\" bi tu choi. Ly do: "
                    + (reason != null && !reason.isBlank() ? reason : "Vui long kiem tra lai noi dung.");
            type = "INSTRUCTOR_COURSE_REJECTED";
        }

        String deepLink = "/instructor/course/" + course.getId() + "/manage";
        notificationService.createNotification(
                instructorAccount.getId(),
                title,
                message,
                deepLink,
                type,
                Notification.NotificationCategory.SYSTEM_NOTIFICATION,
                true,
                course.getTitle(),
                instructorAccount.getFullName()
        );
    }

    private void sendNotificationToEnrolledStudents(Course course, String reason) {
        List<Student> students = enrollmentRepository.findActiveStudentsByCourseId(course.getId());
        String title = "Khoa hoc bi tam khoa";
        String message = "Khoa hoc \"" + course.getTitle() + "\" hien da bi tam khoa"
                + (reason != null && !reason.isBlank() ? ". Ly do: " + reason : ".")
                + " Tien trinh hoc cua ban van duoc luu.";
        String deepLink = "/courses/" + course.getSlug();

        for (Student student : students) {
            notificationService.createNotification(
                    student.getId(),
                    title,
                    message,
                    deepLink,
                    "COURSE_BLOCKED",
                    Notification.NotificationCategory.SYSTEM_NOTIFICATION,
                    true,
                    course.getTitle(),
                    student.getFullName()
            );
        }
    }

    public List<CourseApprovalHistory> getApprovalHistory(Long courseId) {
        return approvalHistoryRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
    }

    @Transactional
    public void updateCourse(Long id, CourseDTO dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học."));

        // Chặn sửa nhanh khi khóa học đang ở trạng thái nhạy cảm
        CourseStatus status = course.getStatus();
        if (status == CourseStatus.PENDING_APPROVAL) {
            throw new RuntimeException("Khóa học đang chờ duyệt. Hãy duyệt hoặc từ chối trước khi sửa giá.");
        }
        if (status == CourseStatus.INACTIVE_REQUESTED) {
            throw new RuntimeException("Khóa học đang chờ duyệt yêu cầu tạm dừng bán. Không thể sửa giá lúc này.");
        }

        // Khoá giá niêm yết khi khoá học đã có học viên trả tiền:
        // triết lý "đã hoàn thành mới bán" — update sau là bonus miễn phí, không tăng giá người đã mua.
        // salePrice (khuyến mãi xuống) vẫn cho phép vì là giảm giá tạm thời, không ảnh hưởng học viên cũ.
        // Lưu ý: kiểm tra theo studentCount, không theo status — khoá đã PUBLISHED rồi thêm bài lớn
        // sẽ bị đẩy về PENDING_APPROVAL nhưng vẫn có học viên cũ, không được mở khoá giá.
        boolean priceLocked = course.getStudentCount() != null && course.getStudentCount() > 0;
        if (priceLocked && dto.getPrice() != null
                && (course.getPrice() == null || dto.getPrice().compareTo(course.getPrice()) != 0)) {
            throw new RuntimeException(
                    "Khóa học đã có học viên đăng ký nên không thể đổi giá niêm yết. "
                            + "Hãy dùng chức năng Sale Campaign để giảm giá khuyến mãi.");
        }

        // Resolve giá sau khi áp dụng dto (giá trị null = giữ nguyên)
        BigDecimal newPrice = dto.getPrice() != null ? dto.getPrice() : course.getPrice();
        BigDecimal newSalePrice = dto.getSalePrice() != null ? dto.getSalePrice() : course.getSalePrice();

        // Validation giá
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Giá niêm yết không hợp lệ.");
        }
        // salePrice = 0 hoặc null = không có sale; nếu > 0 thì phải nhỏ hơn price
        if (newSalePrice != null && newSalePrice.compareTo(BigDecimal.ZERO) > 0
                && newSalePrice.compareTo(newPrice) >= 0) {
            throw new RuntimeException("Giá khuyến mãi phải nhỏ hơn giá niêm yết.");
        }
        if (newSalePrice != null && newSalePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Giá khuyến mãi không hợp lệ.");
        }

        // Validation tiêu đề
        if (dto.getTitle() != null && dto.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Tiêu đề khóa học không được để trống.");
        }

        // Snapshot trước khi đổi để log
        String oldTitle = course.getTitle();
        BigDecimal oldPrice = course.getPrice();
        BigDecimal oldSalePrice = course.getSalePrice();
        Long oldCategoryId = course.getCategory() != null ? course.getCategory().getId() : null;

        if (dto.getTitle() != null) {
            course.setTitle(dto.getTitle().trim());
        }
        if (dto.getPrice() != null) {
            course.setPrice(newPrice);
        }
        if (dto.getSalePrice() != null) {
            // Lưu null nếu salePrice = 0 để biểu thị "không có sale"
            course.setSalePrice(newSalePrice != null && newSalePrice.compareTo(BigDecimal.ZERO) > 0
                    ? newSalePrice : BigDecimal.ZERO);
        }
        if (dto.getCategoryId() != null
                && (oldCategoryId == null || !oldCategoryId.equals(dto.getCategoryId()))) {
            Category cat = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục."));
            course.setCategory(cat);
        }

        courseRepository.save(course);

        // Audit log + notify instructor (chỉ khi PUBLISHED — khóa học đang bán mới ảnh hưởng học viên)
        StringBuilder changes = new StringBuilder();
        if (dto.getTitle() != null && !java.util.Objects.equals(oldTitle, course.getTitle())) {
            changes.append("title: \"").append(oldTitle).append("\" → \"").append(course.getTitle()).append("\"; ");
        }
        if (dto.getPrice() != null && !java.util.Objects.equals(oldPrice, course.getPrice())) {
            changes.append("price: ").append(oldPrice).append(" → ").append(course.getPrice()).append("; ");
        }
        if (dto.getSalePrice() != null && !java.util.Objects.equals(oldSalePrice, course.getSalePrice())) {
            changes.append("salePrice: ").append(oldSalePrice).append(" → ").append(course.getSalePrice()).append("; ");
        }
        if (dto.getCategoryId() != null && !java.util.Objects.equals(oldCategoryId, dto.getCategoryId())) {
            changes.append("categoryId: ").append(oldCategoryId).append(" → ").append(dto.getCategoryId()).append("; ");
        }

        if (changes.length() > 0) {
            adminActionLogService.log(
                    "ADMIN_QUICK_EDIT_COURSE",
                    "Sửa nhanh khóa học \"" + course.getTitle() + "\": " + changes.toString().trim(),
                    course.getId(),
                    "COURSE"
            );

            if (status == CourseStatus.PUBLISHED && course.getInstructor() != null
                    && course.getInstructor().getUser() != null) {
                notificationService.createNotification(
                        course.getInstructor().getUser().getId(),
                        "Admin đã chỉnh sửa khóa học",
                        "Admin vừa cập nhật thông tin khóa học \"" + course.getTitle() + "\": " + changes.toString().trim(),
                        "/instructor/course/" + course.getId() + "/manage",
                        "INSTRUCTOR_COURSE_ADMIN_EDITED",
                        Notification.NotificationCategory.SYSTEM_NOTIFICATION,
                        true,
                        course.getTitle(),
                        course.getInstructor().getUser().getFullName()
                );
            }
        }
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (course.getStatus() == CourseStatus.PUBLISHED || course.getStatus() == CourseStatus.INACTIVE_REQUESTED) {
            throw new RuntimeException("Khong the xoa khoa hoc dang mo ban hoac dang cho duyet tam dung. Hay khoa truoc khi xoa.");
        }

        long enrolled = enrollmentRepository.countByCourse_Id(id);
        if (enrolled > 0) {
            throw new RuntimeException(
                    "Khong the xoa khoa hoc dang co " + enrolled
                            + " hoc vien da dang ky. Hay BLOCK khoa hoc thay vi xoa."
            );
        }

        sendDeleteNotificationToInstructor(course);
        adminActionLogService.log(
                "DELETE_COURSE",
                "Xoa khoa hoc \"" + course.getTitle() + "\"",
                id,
                "COURSE"
        );

        // Dọn FK trỏ tới course trước khi xóa (FK_CampaignItem_Course không CASCADE).
        saleCampaignItemRepository.deleteByCourse_Id(id);

        courseRepository.delete(course);
    }

    private void sendDeleteNotificationToInstructor(Course course) {
        if (course.getInstructor() == null || course.getInstructor().getUser() == null) {
            return;
        }

        Student instructorAccount = course.getInstructor().getUser();
        notificationService.createNotification(
                instructorAccount.getId(),
                "Khoa hoc da bi xoa",
                "Admin da xoa khoa hoc \"" + course.getTitle()
                        + "\" khoi he thong. Neu can mo ban lai, vui long tao khoa hoc moi hoac lien he quan tri.",
                "/instructor/courses",
                "INSTRUCTOR_COURSE_DELETED",
                Notification.NotificationCategory.SYSTEM_NOTIFICATION,
                true,
                course.getTitle(),
                instructorAccount.getFullName()
        );
    }

    private CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setSlug(course.getSlug());
        dto.setThumbnail(course.getThumbnail());
        dto.setPrice(course.getPrice());
        dto.setSalePrice(course.getSalePrice());

        if (course.getStatus() == CourseStatus.PENDING_APPROVAL) {
            dto.setStatus("PENDING");
        } else if (course.getStatus() == CourseStatus.INACTIVE) {
            dto.setStatus("INACTIVE");
        } else if (course.getStatus() == CourseStatus.INACTIVE_REQUESTED) {
            dto.setStatus("INACTIVE_REQUESTED");
        } else {
            dto.setStatus(course.getStatus().name());
        }

        if (course.getCategory() != null) {
            dto.setCategoryId(course.getCategory().getId());
            dto.setCategoryName(course.getCategory().getName());
        }

        try {
            if (course.getInstructor() != null && course.getInstructor().getUser() != null) {
                dto.setAuthorName(course.getInstructor().getUser().getFullName());
            }
        } catch (Exception ignored) {
        }

        long count = course.getStudentCount() != null ? course.getStudentCount() : 0L;
           dto.setStudentCount(count);

        dto.setCreatedAt(course.getCreatedAt());

        // Chỉ gợi ý giá khi giá còn được phép đổi (chưa có học viên trả tiền).
        // Khi đã có học viên: giá đã chốt vĩnh viễn, hiển thị gợi ý "tăng giá" sẽ gây hiểu nhầm
        // (admin click cũng không apply được do guard ở quickEditCourse / approveCourseWithPrice).
        boolean priceStillEditable = count == 0;
        if (priceStillEditable) {
            double basePrice = 150000.0;
            int durationSeconds = course.getTotalDuration() != null ? course.getTotalDuration() : 0;
            int lessons = course.getTotalLessons() != null ? course.getTotalLessons() : 0;
            double durationBonus = (durationSeconds / 3600.0) * 100000.0;
            double lessonBonus = lessons * 10000.0;
            double suggested = basePrice + durationBonus + lessonBonus;
            suggested = Math.ceil(suggested / 10000.0) * 10000.0;
            dto.setSuggestedPrice(java.math.BigDecimal.valueOf(suggested));
        }

        return dto;
    }

    @Transactional
    public void applyBatchSale(org.example.multileanproject.dto.BatchSaleRequest request) {
        if (request.getCourseIds() == null || request.getCourseIds().isEmpty()) {
            throw new RuntimeException("Chua chon khoa hoc nao de ap dung sale.");
        }

        List<Course> courses = courseRepository.findAllById(request.getCourseIds());

        for (Course course : courses) {
            if (course.getStatus() != CourseStatus.PUBLISHED) {
                continue;
            }

            BigDecimal currentPrice = course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO;

            if (request.getSaleValue() == null || request.getSaleValue().compareTo(BigDecimal.ZERO) <= 0) {
                course.setSalePrice(null);
            } else if ("PERCENT".equalsIgnoreCase(request.getSaleType())) {
                double pct = request.getSaleValue().doubleValue();
                if (pct <= 0 || pct >= 100) {
                    throw new RuntimeException("Phan tram giam gia phai nam trong khoang 1-99.");
                }
                double saleAmount = currentPrice.doubleValue() * (pct / 100.0);
                double finalSalePrice = currentPrice.doubleValue() - saleAmount;
                course.setSalePrice(BigDecimal.valueOf(finalSalePrice));
            } else if ("FIXED".equalsIgnoreCase(request.getSaleType())) {
                if (request.getSaleValue().compareTo(currentPrice) >= 0) {
                    throw new RuntimeException("Gia khuyen mai phai nho hon gia goc cua khoa hoc.");
                }
                course.setSalePrice(request.getSaleValue());
            }

            courseRepository.save(course);
        }
    }

    @Transactional
    public void bulkApproveOrReject(CourseBulkApprovalRequest request) {
        if (request == null || request.getCourseIds() == null || request.getCourseIds().isEmpty()) {
            throw new RuntimeException("Vui long chon it nhat 1 khoa hoc.");
        }

        String action = request.getAction() == null ? "" : request.getAction().trim().toUpperCase();
        if (!"APPROVE".equals(action) && !"REJECT".equals(action)) {
            throw new RuntimeException("Action khong hop le. Chi ho tro APPROVE hoac REJECT.");
        }

        String rejectReason = (request.getReason() == null || request.getReason().isBlank())
                ? "Khoa hoc bi tu choi trong luot duyet hang loat."
                : request.getReason().trim();

        for (Long courseId : request.getCourseIds()) {
            if (courseId == null) {
                continue;
            }

            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Khong tim thay khoa hoc ID: " + courseId));
            CourseStatus currentStatus = course.getStatus() != null ? course.getStatus() : CourseStatus.DRAFT;
            if (currentStatus != CourseStatus.PENDING_APPROVAL && currentStatus != CourseStatus.INACTIVE_REQUESTED) {
                throw new RuntimeException("Chi duoc bulk voi khoa hoc dang cho duyet hoac cho duyet tam dung ban. Loi tai ID: " + courseId);
            }

            if ("APPROVE".equals(action)) {
                approveCourseWithPrice(courseId, new CourseApproveRequest());
            } else {
                updateCourseStatus(courseId, "REJECTED", rejectReason);
            }
        }
    }

    private void autoCancelPendingChangeRequests(Long courseId, String reason) {
        try {
            var pending = courseChangeRequestRepository
                    .findAllByCourse_IdAndStatus(courseId, CourseChangeRequestStatus.PENDING);
            if (!pending.isEmpty()) {
                pending.forEach(r -> {
                    r.setStatus(CourseChangeRequestStatus.CANCELED);
                    r.setAdminNote(reason);
                });
                courseChangeRequestRepository.saveAll(pending);
            }
        } catch (Exception ignored) {
            // Không để lỗi auto-cancel ảnh hưởng luồng chính
        }
    }
}
