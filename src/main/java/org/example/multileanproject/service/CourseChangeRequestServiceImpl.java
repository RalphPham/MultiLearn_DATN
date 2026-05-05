package org.example.multileanproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.BasicInfoPayload;
import org.example.multileanproject.dto.CourseChangeRequestDTO;
import org.example.multileanproject.dto.CreateBasicInfoChangeRequestDTO;
import org.example.multileanproject.dto.ReviewCourseChangeRequestDTO;
import org.example.multileanproject.entity.Admin;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.CourseChangeRequest;
import org.example.multileanproject.entity.CourseChangeRequestStatus;
import org.example.multileanproject.entity.CourseStatus;
import org.example.multileanproject.entity.Instructor;
import org.example.multileanproject.entity.Notification;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.repository.AdminRepository;
import org.example.multileanproject.repository.CategoryRepository;
import org.example.multileanproject.repository.CourseChangeRequestRepository;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.InstructorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseChangeRequestServiceImpl implements CourseChangeRequestService {

    private final CourseChangeRequestRepository changeRequestRepository;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final AdminRepository adminRepository;
    private final CategoryRepository categoryRepository;
    private final AdminActionLogService adminActionLogService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CourseChangeRequestDTO createBasicInfoRequest(
            Long courseId, CreateBasicInfoChangeRequestDTO dto, String instructorEmail) {

        Instructor instructor = instructorRepository.findByUser_Email(instructorEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Không tìm thấy thông tin giảng viên."));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy khóa học."));

        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Bạn không có quyền thao tác với khóa học này.");
        }

        CourseStatus status = course.getStatus();
        boolean isLive = status == CourseStatus.PUBLISHED
                || status == CourseStatus.INACTIVE
                || status == CourseStatus.INACTIVE_REQUESTED;
        if (!isLive) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Khóa học đang ở trạng thái có thể sửa trực tiếp, không cần gửi yêu cầu.");
        }

        if (dto.getCategoryId() != null) {
            categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Danh mục không hợp lệ."));
        }

        // Auto-cancel các request PENDING cũ
        List<CourseChangeRequest> pending = changeRequestRepository
                .findAllByCourse_IdAndStatus(courseId, CourseChangeRequestStatus.PENDING);
        if (!pending.isEmpty()) {
            pending.forEach(r -> {
                r.setStatus(CourseChangeRequestStatus.CANCELED);
                r.setAdminNote("Tự động hủy khi giảng viên gửi yêu cầu mới.");
            });
            changeRequestRepository.saveAll(pending);
        }

        // Snapshot trước thay đổi
        BasicInfoPayload before = BasicInfoPayload.builder()
                .title(course.getTitle())
                .categoryId(course.getCategory() != null ? course.getCategory().getId() : null)
                .categoryName(course.getCategory() != null ? course.getCategory().getName() : null)
                .shortDescription(course.getShortDescription())
                .description(course.getDescription())
                .learningOutcomes(course.getLearningOutcomes())
                .language(course.getLanguage())
                .level(course.getLevel())
                .thumbnail(course.getThumbnail())
                .build();

        // Snapshot sau thay đổi — field nào null thì giữ nguyên giá trị hiện tại
        String newCategoryName = null;
        if (dto.getCategoryId() != null) {
            newCategoryName = categoryRepository.findById(dto.getCategoryId())
                    .map(c -> c.getName()).orElse(null);
        } else if (course.getCategory() != null) {
            newCategoryName = course.getCategory().getName();
        }

        BasicInfoPayload after = BasicInfoPayload.builder()
                .title(dto.getTitle() != null ? dto.getTitle() : course.getTitle())
                .categoryId(dto.getCategoryId() != null ? dto.getCategoryId()
                        : (course.getCategory() != null ? course.getCategory().getId() : null))
                .categoryName(newCategoryName)
                .shortDescription(dto.getShortDescription() != null
                        ? dto.getShortDescription() : course.getShortDescription())
                .description(dto.getDescription() != null
                        ? dto.getDescription() : course.getDescription())
                .learningOutcomes(dto.getLearningOutcomes() != null
                        ? dto.getLearningOutcomes() : course.getLearningOutcomes())
                .language(dto.getLanguage() != null ? dto.getLanguage() : course.getLanguage())
                .level(dto.getLevel() != null ? dto.getLevel() : course.getLevel())
                .thumbnail(dto.getThumbnail() != null ? dto.getThumbnail() : course.getThumbnail())
                .build();

        CourseChangeRequest request = CourseChangeRequest.builder()
                .course(course)
                .instructor(instructor)
                .requestType("BASIC_INFO")
                .status(CourseChangeRequestStatus.PENDING)
                .payloadBefore(toJson(before))
                .payloadAfter(toJson(after))
                .requestNote(dto.getRequestNote())
                .build();
        changeRequestRepository.save(request);

        adminActionLogService.log(
                "REQUEST_BASIC_INFO_EDIT",
                "Giảng viên \"" + instructor.getFullName() + "\" gửi yêu cầu chỉnh sửa thông tin cơ bản khóa học \""
                        + course.getTitle() + "\"",
                request.getId(),
                "COURSE_CHANGE_REQUEST"
        );

        return toDTO(request);
    }

    @Override
    @Transactional
    public void cancelRequest(Long requestId, String instructorEmail) {
        CourseChangeRequest request = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy yêu cầu chỉnh sửa."));

        if (!request.getInstructor().getUser().getEmail().equalsIgnoreCase(instructorEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
        }

        if (request.getStatus() != CourseChangeRequestStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Chỉ có thể hủy yêu cầu đang chờ duyệt.");
        }

        request.setStatus(CourseChangeRequestStatus.CANCELED);
        changeRequestRepository.save(request);

        adminActionLogService.log(
                "CANCEL_BASIC_INFO_REQUEST",
                "Giảng viên hủy yêu cầu chỉnh sửa thông tin cơ bản khóa học \""
                        + request.getCourse().getTitle() + "\"",
                requestId,
                "COURSE_CHANGE_REQUEST"
        );
    }

    @Override
    public Optional<CourseChangeRequestDTO> getLatestRequest(Long courseId, String instructorEmail) {
        Instructor instructor = instructorRepository.findByUser_Email(instructorEmail).orElse(null);
        if (instructor == null) return Optional.empty();

        return changeRequestRepository
                .findFirstByCourse_IdAndInstructor_IdOrderByCreatedAtDesc(courseId, instructor.getId())
                .map(this::toDTO);
    }

    @Override
    public Page<CourseChangeRequestDTO> listPendingRequests(int page, int size) {
        return changeRequestRepository
                .findByStatusAndRequestTypeOrderByCreatedAtDesc(
                        CourseChangeRequestStatus.PENDING, "BASIC_INFO", PageRequest.of(page, size))
                .map(this::toDTO);
    }

    @Override
    @Transactional
    public CourseChangeRequestDTO approveRequest(
            Long requestId, ReviewCourseChangeRequestDTO dto, String adminUsername) {

        CourseChangeRequest request = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy yêu cầu chỉnh sửa."));

        if (request.getStatus() != CourseChangeRequestStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Yêu cầu này đã được xử lý trước đó.");
        }

        Admin admin = adminRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Không tìm thấy thông tin admin."));

        // Apply dữ liệu mới vào course
        Course course = request.getCourse();
        applyPayloadToCourse(course, fromJson(request.getPayloadAfter()));
        courseRepository.save(course);

        request.setStatus(CourseChangeRequestStatus.APPROVED);
        request.setAdminNote(dto != null ? dto.getAdminNote() : null);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        changeRequestRepository.save(request);

        adminActionLogService.log(
                "APPROVE_BASIC_INFO_EDIT",
                "Admin \"" + admin.getUsername() + "\" duyệt yêu cầu chỉnh sửa thông tin cơ bản khóa học \""
                        + course.getTitle() + "\"",
                requestId,
                "COURSE_CHANGE_REQUEST"
        );

        sendNotificationToInstructor(request, true, null);

        return toDTO(request);
    }

    @Override
    @Transactional
    public CourseChangeRequestDTO rejectRequest(
            Long requestId, ReviewCourseChangeRequestDTO dto, String adminUsername) {

        CourseChangeRequest request = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy yêu cầu chỉnh sửa."));

        if (request.getStatus() != CourseChangeRequestStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Yêu cầu này đã được xử lý trước đó.");
        }

        if (dto == null || dto.getAdminNote() == null || dto.getAdminNote().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Vui lòng nhập lý do từ chối.");
        }

        Admin admin = adminRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Không tìm thấy thông tin admin."));

        // Không đụng dữ liệu course
        request.setStatus(CourseChangeRequestStatus.REJECTED);
        request.setAdminNote(dto.getAdminNote());
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        changeRequestRepository.save(request);

        adminActionLogService.log(
                "REJECT_BASIC_INFO_EDIT",
                "Admin \"" + admin.getUsername() + "\" từ chối yêu cầu chỉnh sửa thông tin cơ bản khóa học \""
                        + request.getCourse().getTitle() + "\"",
                requestId,
                "COURSE_CHANGE_REQUEST"
        );

        sendNotificationToInstructor(request, false, dto.getAdminNote());

        return toDTO(request);
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private void applyPayloadToCourse(Course course, BasicInfoPayload payload) {
        if (payload.getTitle() != null)            course.setTitle(payload.getTitle());
        if (payload.getShortDescription() != null) course.setShortDescription(payload.getShortDescription());
        if (payload.getDescription() != null)      course.setDescription(payload.getDescription());
        if (payload.getLearningOutcomes() != null) course.setLearningOutcomes(payload.getLearningOutcomes());
        if (payload.getLanguage() != null)         course.setLanguage(payload.getLanguage());
        if (payload.getLevel() != null)            course.setLevel(payload.getLevel());
        if (payload.getThumbnail() != null)        course.setThumbnail(payload.getThumbnail());
        if (payload.getCategoryId() != null) {
            categoryRepository.findById(payload.getCategoryId()).ifPresent(course::setCategory);
        }
    }

    private void sendNotificationToInstructor(CourseChangeRequest request, boolean approved, String adminNote) {
        try {
            Student instructorAccount = request.getInstructor().getUser();
            if (instructorAccount == null) return;

            Course course = request.getCourse();
            String title;
            String message;
            String type;

            if (approved) {
                title = "Yêu cầu chỉnh sửa được duyệt";
                message = "Yêu cầu chỉnh sửa thông tin khóa học \""
                        + course.getTitle() + "\" đã được duyệt và áp dụng.";
                type = "INSTRUCTOR_BASIC_INFO_APPROVED";
            } else {
                title = "Yêu cầu chỉnh sửa bị từ chối";
                message = "Yêu cầu chỉnh sửa thông tin khóa học \""
                        + course.getTitle() + "\" bị từ chối."
                        + (adminNote != null && !adminNote.isBlank() ? " Lý do: " + adminNote : "");
                type = "INSTRUCTOR_BASIC_INFO_REJECTED";
            }

            notificationService.createNotification(
                    instructorAccount.getId(),
                    title,
                    message,
                    "/instructor/courses/" + course.getId() + "/manage",
                    type,
                    Notification.NotificationCategory.SYSTEM_NOTIFICATION,
                    true,
                    course.getTitle(),
                    instructorAccount.getFullName()
            );
        } catch (Exception ignored) {
            // Không để lỗi notification ảnh hưởng luồng chính
        }
    }

    private String toJson(BasicInfoPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi xử lý dữ liệu yêu cầu.");
        }
    }

    private BasicInfoPayload fromJson(String json) {
        try {
            return objectMapper.readValue(json, BasicInfoPayload.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi đọc dữ liệu yêu cầu.");
        }
    }

    private CourseChangeRequestDTO toDTO(CourseChangeRequest req) {
        BasicInfoPayload before = null;
        BasicInfoPayload after = null;
        try {
            if (req.getPayloadBefore() != null) before = fromJson(req.getPayloadBefore());
            if (req.getPayloadAfter() != null)  after  = fromJson(req.getPayloadAfter());
        } catch (Exception ignored) {}

        return CourseChangeRequestDTO.builder()
                .id(req.getId())
                .courseId(req.getCourse().getId())
                .courseTitle(req.getCourse().getTitle())
                .courseSlug(req.getCourse().getSlug())
                .instructorId(req.getInstructor().getId())
                .instructorName(req.getInstructor().getFullName())
                .requestType(req.getRequestType())
                .status(req.getStatus().name())
                .payloadBefore(before)
                .payloadAfter(after)
                .requestNote(req.getRequestNote())
                .adminNote(req.getAdminNote())
                .reviewedById(req.getReviewedBy() != null ? req.getReviewedBy().getId() : null)
                .reviewedByName(req.getReviewedBy() != null ? req.getReviewedBy().getFullName() : null)
                .reviewedAt(req.getReviewedAt())
                .createdAt(req.getCreatedAt())
                .updatedAt(req.getUpdatedAt())
                .build();
    }
}
