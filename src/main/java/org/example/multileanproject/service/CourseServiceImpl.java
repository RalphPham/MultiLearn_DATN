package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.CourseDetailDTO;
import org.example.multileanproject.dto.CourseListDTO;
import org.example.multileanproject.dto.CourseRequestDTO;
import org.example.multileanproject.dto.InstructorCourseListItemDTO;
import org.example.multileanproject.dto.LessonRequestDTO;
import org.example.multileanproject.dto.SectionRequestDTO;
import org.example.multileanproject.entity.Category;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.CourseStatus;
import org.example.multileanproject.entity.Enrollment;
import org.example.multileanproject.entity.Instructor;
import org.example.multileanproject.entity.Lesson;
import org.example.multileanproject.entity.LessonType;
import org.example.multileanproject.entity.Quiz;
import org.example.multileanproject.entity.Section;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.entity.SaleCampaignItem;
import org.example.multileanproject.entity.Notification;
import org.example.multileanproject.repository.CategoryRepository;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.EnrollmentRepository;
import org.example.multileanproject.repository.InstructorRepository;
import org.example.multileanproject.repository.LearningProgressRepository;
import org.example.multileanproject.repository.LessonRepository;
import org.example.multileanproject.repository.OrderRepository;
import org.example.multileanproject.repository.QuizRepository;
import org.example.multileanproject.repository.RefundRequestRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.repository.SaleCampaignItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final NotificationService notificationService;
    private final InstructorRepository instructorRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizRepository quizRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    private final RefundRequestRepository refundRequestRepository;
    private final SaleCampaignItemRepository campaignItemRepository;
    private final AdminActionLogService adminActionLogService;

    @Override
    @Transactional(readOnly = true)
    public List<InstructorCourseListItemDTO> getMyCourses(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email người dùng không hợp lệ.");
        }

        Student student = studentRepository.findByEmail(email.trim())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email: " + email));

        Instructor instructor = instructorRepository.findByUser_Id(student.getId())
                .orElseThrow(() -> new RuntimeException("Tài khoản này chưa có hồ sơ giảng viên."));

        List<Course> courses = courseRepository.findByInstructorIdWithDetails(instructor.getId());

        return courses.stream()
                .map(course -> InstructorCourseListItemDTO.builder()
                        .id(course.getId())
                        .title(course.getTitle())
                        .thumbnail(course.getThumbnail())
                        .price(course.getPrice())
                        .salePrice(course.getSalePrice())
                        .status(course.getStatus() != null ? course.getStatus().name() : "DRAFT")
                        .createdAt(course.getCreatedAt())
                        .updatedAt(course.getUpdatedAt())
                        .categoryId(course.getCategory() != null ? course.getCategory().getId() : null)
                        .categoryName(course.getCategory() != null ? course.getCategory().getName() : null)
                        .totalLessons(course.getTotalLessons())
                        .totalDuration(course.getTotalDuration())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Course createCourseWithInstructor(Course course, String email) {
        Instructor instructor = instructorRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Tài khoản của bạn chưa được cấp quyền Giảng viên!"));

        course.setInstructor(instructor);

        if (course.getStatus() == null) {
            course.setStatus(CourseStatus.DRAFT);
        }

        if (course.getSlug() == null || course.getSlug().isEmpty()) {
            String base = "course-" + System.currentTimeMillis();
            String slug = base;
            int attempt = 1;
            while (courseRepository.existsBySlug(slug)) {
                slug = base + "-" + attempt++;
            }
            course.setSlug(slug);
        }

        if (course.getPrice() == null) {
            course.setPrice(BigDecimal.ZERO);
        }

        if (course.getSalePrice() == null) {
            course.setSalePrice(BigDecimal.ZERO);
        }

        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course updateCourse(Long courseId, CourseRequestDTO request) {
        Course existingCourse = getOwnedCourseOrAdmin(courseId);
        CourseStatus requestedStatus = null;
        CourseStatus statusBeforeUpdate = existingCourse.getStatus() != null
                ? existingCourse.getStatus()
                : CourseStatus.DRAFT;
        boolean majorEditRequiresReapproval =
                statusBeforeUpdate == CourseStatus.PUBLISHED && hasMajorContentChange(existingCourse, request);
        boolean majorReviewRequested = false;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = isAdmin(authentication);

        if (!isAdmin && request.getSections() != null) {
            ensureNoHardDeleteOnEnrolledCourse(existingCourse, request);
        }

        // Guard: PUBLISHED/INACTIVE/INACTIVE_REQUESTED — instructor không được sửa trực tiếp basic info.
        // Ngoại lệ: nếu instructor đang gửi duyệt lại (target = PENDING_APPROVAL) từ PUBLISHED hoặc INACTIVE
        // thì cho phép thay đổi basic info, vì khóa học sẽ chuyển về chờ admin duyệt.
        if (!isAdmin) {
            boolean isLive = statusBeforeUpdate == CourseStatus.PUBLISHED
                    || statusBeforeUpdate == CourseStatus.INACTIVE
                    || statusBeforeUpdate == CourseStatus.INACTIVE_REQUESTED;
            boolean submittingForReview = "PENDING_APPROVAL".equals(request.getStatus())
                    && (statusBeforeUpdate == CourseStatus.PUBLISHED
                        || statusBeforeUpdate == CourseStatus.INACTIVE);
            if (isLive && !submittingForReview && hasBasicInfoChange(existingCourse, request)) {
                // INACTIVE_REQUESTED: đang chờ duyệt tạm dừng bán -> vẫn chặn để không xung đột flow
                if (statusBeforeUpdate == CourseStatus.INACTIVE_REQUESTED) {
                    throw new org.springframework.web.server.ResponseStatusException(
                            org.springframework.http.HttpStatus.FORBIDDEN,
                            "Khóa học đang chờ admin duyệt yêu cầu tạm dừng bán. "
                          + "Vui lòng đợi admin xử lý trước khi chỉnh sửa.");
                }
                // PUBLISHED / INACTIVE: tự động chuyển sang PENDING_APPROVAL để admin duyệt lại,
                // thay vì bắt instructor phải nhớ tự set status. UX mềm hơn, vẫn an toàn vì
                // logic xử lý status ở dưới sẽ chuyển khoá học sang trạng thái chờ duyệt.
                request.setStatus(CourseStatus.PENDING_APPROVAL.name());
            }
        }

        existingCourse.setTitle(request.getTitle());
        existingCourse.setShortDescription(request.getShortDescription());
        existingCourse.setDescription(request.getDescription());
        existingCourse.setThumbnail(request.getThumbnail());
        existingCourse.setLevel(request.getLevel());
        existingCourse.setLanguage(request.getLanguage());

        try {
            existingCourse.setLearningOutcomes(request.getLearningOutcomes());
        } catch (Exception ignored) {
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId()).orElse(null);
            existingCourse.setCategory(category);
        } else {
            existingCourse.setCategory(null);
        }

        if (request.getStatus() != null) {
            try {
                requestedStatus = CourseStatus.valueOf(request.getStatus());
                CourseStatus currentStatus = statusBeforeUpdate;

                if (currentStatus == CourseStatus.BLOCKED
                        && (requestedStatus == CourseStatus.PUBLISHED
                        || requestedStatus == CourseStatus.INACTIVE
                        || requestedStatus == CourseStatus.INACTIVE_REQUESTED)) {
                    throw new RuntimeException("Khoa hoc dang bi admin khoa, khong the tu doi trang thai thuong mai.");
                }

                if (!isAdmin) {
                    if (currentStatus == CourseStatus.INACTIVE_REQUESTED
                            && requestedStatus != CourseStatus.INACTIVE_REQUESTED) {
                        throw new RuntimeException(
                                "Khoa hoc dang cho admin duyet yeu cau tam dung ban. " +
                                "Khong the doi trang thai sang " + requestedStatus + "."
                        );
                    }

                    if (requestedStatus == CourseStatus.PUBLISHED) {
                        if (currentStatus == CourseStatus.INACTIVE) {
                            existingCourse.setStatus(CourseStatus.PENDING_APPROVAL);
                            logInstructorCommercialAction(
                                    existingCourse,
                                    "REQUEST_REOPEN_SALE",
                                    "Giang vien gui yeu cau mo ban lai khoa hoc \"" + existingCourse.getTitle() + "\""
                            );
                        } else if (currentStatus == CourseStatus.PUBLISHED && majorEditRequiresReapproval) {
                            existingCourse.setStatus(CourseStatus.PENDING_APPROVAL);
                            logInstructorCommercialAction(
                                    existingCourse,
                                    "REQUEST_MAJOR_EDIT_REVIEW",
                                    "Giang vien cap nhat noi dung lon, gui duyet lai khoa hoc \"" + existingCourse.getTitle() + "\""
                            );
                            majorReviewRequested = true;
                        } else {
                            existingCourse.setStatus(CourseStatus.PUBLISHED);
                        }
                    } else if (requestedStatus == CourseStatus.INACTIVE || requestedStatus == CourseStatus.INACTIVE_REQUESTED) {
                        if (currentStatus != CourseStatus.PUBLISHED && currentStatus != CourseStatus.INACTIVE_REQUESTED) {
                            throw new RuntimeException("Chi co the gui yeu cau tam dung ban khi khoa hoc dang o trang thai PUBLISHED.");
                        }
                        existingCourse.setStatus(CourseStatus.INACTIVE_REQUESTED);
                        if (currentStatus != CourseStatus.INACTIVE_REQUESTED) {
                            logInstructorCommercialAction(
                                    existingCourse,
                                    "REQUEST_PAUSE_SALE",
                                    "Giang vien gui yeu cau tam dung ban khoa hoc \"" + existingCourse.getTitle() + "\""
                            );
                        }
                    } else if (requestedStatus == CourseStatus.PENDING_APPROVAL) {
                        existingCourse.setStatus(CourseStatus.PENDING_APPROVAL);
                        if (currentStatus == CourseStatus.PUBLISHED && majorEditRequiresReapproval) {
                            logInstructorCommercialAction(
                                    existingCourse,
                                    "REQUEST_MAJOR_EDIT_REVIEW",
                                    "Giang vien cap nhat noi dung lon, gui duyet lai khoa hoc \"" + existingCourse.getTitle() + "\""
                            );
                            majorReviewRequested = true;
                        } else if (currentStatus != CourseStatus.PENDING_APPROVAL) {
                            logInstructorCommercialAction(
                                    existingCourse,
                                    "REQUEST_PUBLISH_REVIEW",
                                    "Giang vien gui yeu cau duyet/xuat ban khoa hoc \"" + existingCourse.getTitle() + "\""
                            );
                        }
                    } else if (requestedStatus == CourseStatus.BLOCKED) {
                        throw new RuntimeException("Giang vien khong co quyen khoa khoa hoc.");
                    } else {
                        existingCourse.setStatus(requestedStatus);
                    }
                } else {
                    existingCourse.setStatus(requestedStatus);
                }

            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Trang thai khoa hoc khong hop le: " + request.getStatus());
            }
        }

        // Backend enforce: published course with major content edits must return to approval,
        // even if FE forgets to send explicit status transition.
        if (!isAdmin
                && majorEditRequiresReapproval
                && !majorReviewRequested
                && existingCourse.getStatus() == CourseStatus.PUBLISHED) {
            existingCourse.setStatus(CourseStatus.PENDING_APPROVAL);
            logInstructorCommercialAction(
                    existingCourse,
                    "REQUEST_MAJOR_EDIT_REVIEW",
                    "Giang vien cap nhat noi dung lon, gui duyet lai khoa hoc \"" + existingCourse.getTitle() + "\""
            );
        }
        if (request.getSections() != null) {
            Map<Long, Section> existingSectionMap = existingCourse.getSections() == null
                    ? Map.of()
                    : existingCourse.getSections().stream()
                    .filter(s -> s.getId() != null)
                    .collect(Collectors.toMap(Section::getId, s -> s));

            List<Section> updatedSections = new ArrayList<>();
            int sectionOrder = 1;

            for (SectionRequestDTO secDTO : request.getSections()) {
                Section section;

                if (secDTO.getId() != null && existingSectionMap.containsKey(secDTO.getId())) {
                    section = existingSectionMap.get(secDTO.getId());
                } else {
                    section = new Section();
                    section.setCourse(existingCourse);
                }

                section.setCourse(existingCourse);
                section.setTitle(secDTO.getTitle());
                section.setOrderIndex(sectionOrder++);

                Map<Long, Lesson> existingLessonMap = section.getLessons() != null
                        ? section.getLessons().stream()
                        .filter(l -> l.getId() != null)
                        .collect(Collectors.toMap(Lesson::getId, l -> l))
                        : Map.of();

                List<Lesson> updatedLessons = new ArrayList<>();
                int lessonOrder = 1;

                if (secDTO.getLessons() != null) {
                    for (LessonRequestDTO lesDTO : secDTO.getLessons()) {
                        Lesson lesson;

                        if (lesDTO.getId() != null && existingLessonMap.containsKey(lesDTO.getId())) {
                            lesson = existingLessonMap.get(lesDTO.getId());
                        } else {
                            lesson = new Lesson();
                        }

                        lesson.setSection(section);
                        lesson.setTitle(lesDTO.getTitle());
                        lesson.setOrderIndex(lessonOrder++);
                        lesson.setIsPreview(lesDTO.getIsPreview() != null ? lesDTO.getIsPreview() : false);

                        LessonType lessonType = parseLessonType(lesDTO.getType());
                        lesson.setType(lessonType);

                        if (lessonType == LessonType.QUIZ) {
                            if (lesDTO.getQuizId() == null) {
                                throw new RuntimeException("Bài học quiz bắt buộc phải có quizId.");
                            }

                            Quiz quiz = quizRepository.findById(lesDTO.getQuizId())
                                    .orElseThrow(() -> new RuntimeException("Không tìm thấy quiz ID: " + lesDTO.getQuizId()));

                            lesson.setQuiz(quiz);
                            lesson.setDuration(0);
                            lesson.setVideoUrl(null);
                            lesson.setContentText(null);
                            lesson.setDocumentUrl(null);
                        } else {
                            lesson.setQuiz(null);
                            lesson.setDuration(lesDTO.getDuration() != null ? lesDTO.getDuration() : 0);
                            lesson.setVideoUrl(lesDTO.getVideoUrl());
                            lesson.setContentText(lesDTO.getContentText());
                            lesson.setDocumentUrl(lesDTO.getDocumentUrl());
                        }

                        updatedLessons.add(lesson);
                    }
                }

                if (section.getLessons() == null) {
                    section.setLessons(new ArrayList<>());
                } else {
                    section.getLessons().clear();
                }

                section.getLessons().addAll(updatedLessons);
                updatedSections.add(section);
            }

            if (existingCourse.getSections() == null) {
                existingCourse.setSections(new ArrayList<>());
            } else {
                existingCourse.getSections().clear();
            }

            existingCourse.getSections().addAll(updatedSections);
        }

        CourseStatus targetStatus = requestedStatus != null ? requestedStatus : existingCourse.getStatus();
        if (targetStatus == CourseStatus.PENDING_APPROVAL) {
            long totalLessons = resolveLessonCountForSubmission(existingCourse, request);
            if (totalLessons < 1) {
                throw new RuntimeException("Khoa hoc phai co it nhat 1 bai hoc truoc khi gui duyet.");
            }
        }

        existingCourse.recalculateTotals();
        return courseRepository.saveAndFlush(existingCourse);
    }

    private LessonType parseLessonType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return LessonType.VIDEO;
        }

        try {
            return LessonType.valueOf(rawType.trim().toUpperCase());
        } catch (Exception e) {
            return LessonType.VIDEO;
        }
    }

    @Override
    public void deleteCourse(Long courseId) {
        Course course = getOwnedCourseOrAdmin(courseId);
        if (course.getStatus() == CourseStatus.PUBLISHED) {
            throw new RuntimeException("Không thể xóa khóa học đang bán. Vui lòng ngừng bán trước.");
        }
        long enrolledCount = enrollmentRepository.countByCourse_Id(courseId);
        if (enrolledCount > 0) {
            throw new RuntimeException("Không thể xóa khóa học đang có " + enrolledCount + " học viên đã đăng ký.");
        }
        courseRepository.delete(course);
    }

    @Override
    @Transactional
    public void toggleSaleStatus(Long courseId, String email) {
        Course course = getOwnedCourseOrAdmin(courseId);
        if (course.getStatus() == CourseStatus.PUBLISHED) {
            course.setStatus(CourseStatus.INACTIVE);
        } else if (course.getStatus() == CourseStatus.INACTIVE) {
            course.setStatus(CourseStatus.PUBLISHED);
        } else {
            throw new RuntimeException("Chỉ có thể đổi trạng thái với khóa học đang bán hoặc đã ngừng bán.");
        }
        courseRepository.save(course);
    }

    @Override
    @Transactional
    public void submitForApproval(Long courseId) {
        Course course = getOwnedCourseOrAdmin(courseId);
        if (course.getStatus() != CourseStatus.DRAFT && course.getStatus() != CourseStatus.REJECTED) {
            throw new RuntimeException("Chỉ có thể gửi duyệt khóa học đang ở trạng thái DRAFT hoặc REJECTED. Trạng thái hiện tại: " + course.getStatus());
        }
        long totalLessons = lessonRepository.countBySection_Course_Id(courseId);
        if (totalLessons < 1) {
            throw new RuntimeException("Khóa học phải có ít nhất 1 bài học trước khi gửi duyệt.");
        }
        course.setStatus(CourseStatus.PENDING_APPROVAL);
        courseRepository.save(course);

        notificationService.createNotification(
                course.getInstructor().getUser().getId(),
                "Đã gửi yêu cầu duyệt khóa học",
                "Khóa học '" + course.getTitle() + "' đang được chờ duyệt.",
                "/instructor/courses",
                "INSTRUCTOR_COURSE_SUBMITTED",
                Notification.NotificationCategory.SYSTEM_NOTIFICATION,
                false,
                course.getTitle(),
                null
        );
    }

    private long resolveLessonCountForSubmission(Course course, CourseRequestDTO request) {
        if (request != null && request.getSections() != null) {
            return request.getSections().stream()
                    .filter(Objects::nonNull)
                    .mapToLong(section -> {
                        if (section.getLessons() == null) {
                            return 0L;
                        }
                        return section.getLessons().stream().filter(Objects::nonNull).count();
                    })
                    .sum();
        }

        if (course == null || course.getId() == null) {
            return 0L;
        }

        return lessonRepository.countBySection_Course_Id(course.getId());
    }

    @Override
    public Course createCourse(Course course) {
        if (course.getStatus() == null) {
            course.setStatus(CourseStatus.DRAFT);
        }
        return courseRepository.save(course);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailDTO getLearningCourseDetail(Long courseId, String email) {
        Course course = courseRepository.findByIdWithDetails(courseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học ID: " + courseId));

        boolean isInstructorOwner = isInstructorOwner(course, email);

        SaleCampaignItem activeSale = campaignItemRepository.findActiveSaleForCourse(course.getId(), LocalDateTime.now()).orElse(null);
        CourseDetailDTO dto = mapToDetailDTO(course, email, activeSale);

        // ÃƒÂ°Ã…Â¸Ã¢â‚¬ÂÃ‚Â¥ Ãƒâ€žÃ‚ÂÃƒÆ’Ã†â€™ SÃƒÂ¡Ã‚Â»Ã‚Â¬A CHÃƒÂ¡Ã‚Â»Ã¢â‚¬â€œ NÃƒÆ’Ã¢â€šÂ¬Y: GiÃƒÂ¡Ã‚ÂºÃ‚Â£ng viÃƒÆ’Ã‚Âªn Ãƒâ€žÃ¢â‚¬ËœÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã‚Â£c cÃƒÂ¡Ã‚ÂºÃ‚Â¥p quyÃƒÂ¡Ã‚Â»Ã‚Ân isOwned = true Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ vÃƒÆ’Ã‚Â o xem video thoÃƒÂ¡Ã‚ÂºÃ‚Â£i mÃƒÆ’Ã‚Â¡i ÃƒÂ°Ã…Â¸Ã¢â‚¬ÂÃ‚Â¥
        if (isInstructorOwner) {
            dto.setIsOwned(true); // <--- QUAN TRÃƒÂ¡Ã‚Â»Ã…â€™NG NHÃƒÂ¡Ã‚ÂºÃ‚Â¤T LÃƒÆ’Ã¢â€šÂ¬ DÃƒÆ’Ã¢â‚¬â„¢NG NÃƒÆ’Ã¢â€šÂ¬Y
            dto.setIsInstructorOwner(true);
            return dto;
        }

        Enrollment enrollment = enrollmentRepository.findByStudent_EmailAndCourse_Id(email, courseId)
                .orElseThrow(() -> new RuntimeException("Bạn chưa đăng ký khóa học này."));

        if (!"ACTIVE".equals(enrollment.getStatus())) {
            throw new RuntimeException("Bạn chưa đăng ký khóa học này.");
        }

        if (refundRequestRepository.countPendingRefundByStudentAndCourse(enrollment.getStudent().getId(), courseId) > 0) {
            throw new RuntimeException("Khoa hoc dang co yeu cau hoan tien cho duyet. Tam thoi khong the tiep tuc hoc.");
        }

        // KiÃƒÂ¡Ã‚Â»Ã†â€™m tra hÃƒÂ¡Ã‚ÂºÃ‚Â¿t hÃƒÂ¡Ã‚ÂºÃ‚Â¡n thuÃƒÆ’Ã‚Âª
        if (enrollment.getRentalExpiresAt() != null && enrollment.getRentalExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("RENTAL_EXPIRED");
        }

        dto.setIsOwned(true);
        dto.setIsInstructorOwner(false);
        dto.setRentalExpiresAt(enrollment.getRentalExpiresAt());
        dto.setIsCourseCompleted(Boolean.TRUE.equals(enrollment.getIsCourseCompleted()));
        dto.setCertificateIssued(Boolean.TRUE.equals(enrollment.getCertificateIssued()));

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học viên."));

        List<Long> completedLessonIds = learningProgressRepository.findCompletedLessonIds(student.getId(), courseId);
        Set<Long> completedLessonIdSet = new HashSet<>(completedLessonIds);

        if (dto.getSections() != null) {
            for (CourseDetailDTO.SectionDTO sectionDTO : dto.getSections()) {
                if (sectionDTO.getLessons() != null) {
                    for (CourseDetailDTO.LessonDTO lessonDTO : sectionDTO.getLessons()) {
                        boolean completed = lessonDTO.getId() != null && completedLessonIdSet.contains(lessonDTO.getId());
                        lessonDTO.setIsCompleted(completed);
                    }
                }
            }
        }

        return dto;
    }

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Page<CourseListDTO> searchCoursesPublic(Map<String, String> params, int page, int size) {
        // 1. LÃƒÂ¡Ã‚ÂºÃ‚Â¥y tÃƒÂ¡Ã‚Â»Ã‚Â« khÃƒÆ’Ã‚Â³a (Ãƒâ€ Ã‚Â°u tiÃƒÆ’Ã‚Âªn "keyword", khÃƒÆ’Ã‚Â´ng cÃƒÆ’Ã‚Â³ thÃƒÆ’Ã‚Â¬ lÃƒÂ¡Ã‚ÂºÃ‚Â¥y "q")
        String keyword = params.getOrDefault("keyword", params.get("q"));
        if (keyword != null) {
            keyword = keyword.trim();
        }
        if ("null".equalsIgnoreCase(keyword)) {
            keyword = ""; // XÃƒÂ¡Ã‚Â»Ã‚Â­ lÃƒÆ’Ã‚Â½ nÃƒÂ¡Ã‚ÂºÃ‚Â¿u Frontend gÃƒÂ¡Ã‚Â»Ã‚Â­i chÃƒÂ¡Ã‚Â»Ã‚Â¯ "null"
        }

        // 2. LÃƒÂ¡Ã‚Â»Ã‚Âc theo Danh mÃƒÂ¡Ã‚Â»Ã‚Â¥c
        Long categoryId = null;
        try {
            if (params.get("categoryId") != null && !params.get("categoryId").equals("null")) {
                categoryId = Long.parseLong(params.get("categoryId"));
            }
        } catch (NumberFormatException ignored) {}

        // 3. LÃƒÂ¡Ã‚Â»Ã‚Âc theo KhoÃƒÂ¡Ã‚ÂºÃ‚Â£ng giÃƒÆ’Ã‚Â¡
        BigDecimal minPrice = null;
        if (params.get("minPrice") != null && !params.get("minPrice").isBlank()) {
            try { minPrice = new BigDecimal(params.get("minPrice")); } catch (Exception ignored) {}
        }

        BigDecimal maxPrice = null;
        if (params.get("maxPrice") != null && !params.get("maxPrice").isBlank()) {
            try { maxPrice = new BigDecimal(params.get("maxPrice")); } catch (Exception ignored) {}
        }

        // ÃƒÂ°Ã…Â¸Ã¢â‚¬ÂÃ‚Â¥ BÃƒÂ¡Ã‚Â»Ã‹Å“ LÃƒÂ¡Ã‚Â»Ã…â€™C MIÃƒÂ¡Ã‚Â»Ã¢â‚¬Å¾N PHÃƒÆ’Ã‚Â: ÃƒÆ’Ã¢â‚¬Â°p giÃƒÆ’Ã‚Â¡ tÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœi Ãƒâ€žÃ¢â‚¬Ëœa vÃƒÂ¡Ã‚Â»Ã‚Â 0 nÃƒÂ¡Ã‚ÂºÃ‚Â¿u cÃƒÆ’Ã‚Â³ params free=true
        if ("true".equalsIgnoreCase(params.get("free"))) {
            maxPrice = BigDecimal.ZERO;
        }

        // 4. SÃƒÂ¡Ã‚ÂºÃ‚Â¯p xÃƒÂ¡Ã‚ÂºÃ‚Â¿p (Sort)
        Sort sortObj = Sort.unsorted();
        if (params.containsKey("sort") && !params.get("sort").isBlank()) {
            String sortParam = params.get("sort");
            String[] parts = sortParam.split(",");
            String property = parts[0];
            Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            sortObj = Sort.by(direction, property);
        } else if (keyword != null && !keyword.isEmpty()) {
            // CÃƒÆ’Ã‚Â³ tÃƒÆ’Ã‚Â¬m kiÃƒÂ¡Ã‚ÂºÃ‚Â¿m -> Ãƒâ€ Ã‚Â¯u tiÃƒÆ’Ã‚Âªn hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n khÃƒÆ’Ã‚Â³a Bestseller lÃƒÆ’Ã‚Âªn trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc
            sortObj = Sort.by(Sort.Direction.DESC, "studentCount");
        } else {
            // MÃƒÂ¡Ã‚ÂºÃ‚Â·c Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹nh -> MÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi nhÃƒÂ¡Ã‚ÂºÃ‚Â¥t
            sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);

        // 5. Truy vÃƒÂ¡Ã‚ÂºÃ‚Â¥n DB
        Page<Course> coursePage = courseRepository.searchCoursesPublic(keyword, minPrice, maxPrice, categoryId, pageable);

        return coursePage.map(course -> {
            SaleCampaignItem activeSale = campaignItemRepository.findActiveSaleForCourse(course.getId(), LocalDateTime.now()).orElse(null);
            return CourseListDTO.fromEntity(course, activeSale);
        });
    }
    @Override
    @Transactional(readOnly = true)
    public CourseDetailDTO getCourseDetailBySlug(String slug, String currentEmail) {
        Course course = courseRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học: " + slug));

        SaleCampaignItem activeSale = campaignItemRepository.findActiveSaleForCourse(course.getId(), LocalDateTime.now()).orElse(null);
        return mapToDetailDTO(course, currentEmail, activeSale);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailDTO getCourseDetail(Long courseId, String currentEmail) {
        Course course = courseRepository.findByIdWithDetails(courseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học ID: " + courseId));

        SaleCampaignItem activeSale = campaignItemRepository.findActiveSaleForCourse(course.getId(), LocalDateTime.now()).orElse(null);
        return mapToDetailDTO(course, currentEmail, activeSale);
    }

    private CourseDetailDTO mapToDetailDTO(Course course, String currentEmail, SaleCampaignItem activeSale) {
        CourseDetailDTO dto = new CourseDetailDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setSlug(course.getSlug());
        dto.setDescription(course.getDescription());
        dto.setShortDescription(course.getShortDescription());
        dto.setThumbnail(course.getThumbnail());
        dto.setPrice(course.getPrice());

        dto.setUpdatedAt(course.getUpdatedAt());
        dto.setLevel(course.getLevel());
        dto.setLanguage(course.getLanguage());
        dto.setLearningOutcomes(course.getLearningOutcomes());
        dto.setStatus(course.getStatus() != null ? course.getStatus().name() : "DRAFT");
        dto.setAverageRating(course.getAverageRating() != null ? course.getAverageRating() : 0.0);
        dto.setStudentCount(course.getStudentCount() != null ? course.getStudentCount() : 0);

        if (activeSale != null) {
            dto.setSalePrice(activeSale.getPromotionalPrice());
            dto.setTotalSlots(activeSale.getTotalSlots());
            dto.setSoldSlots(activeSale.getSoldSlots());
            dto.setIsFlashSale(true);
        } else {
            dto.setSalePrice(course.getSalePrice());
            dto.setIsFlashSale(false);
        }

        boolean isInstructorOwner = false;
        boolean isOwned = false;

        if (course.getInstructor() != null && course.getInstructor().getUser() != null) {
            dto.setInstructorName(course.getInstructor().getUser().getFullName());
            dto.setInstructorAvatar(course.getInstructor().getUser().getAvatar());
            dto.setAuthorName(course.getInstructor().getUser().getFullName());
            dto.setAuthorAvatar(course.getInstructor().getUser().getAvatar());
            dto.setInstructorUserId(course.getInstructor().getUser().getId());

            if (currentEmail != null
                    && course.getInstructor().getUser().getEmail() != null
                    && currentEmail.equalsIgnoreCase(course.getInstructor().getUser().getEmail())) {
                isInstructorOwner = true;
            }
        }

        if (course.getCategory() != null) {
            dto.setCategoryId(course.getCategory().getId());
            dto.setCategoryName(course.getCategory().getName());
        }

        if (currentEmail != null && !currentEmail.isBlank() && !isInstructorOwner) {
            isOwned = hasCourseOwnership(currentEmail, course.getId());
        }

        dto.setIsOwned(isOwned);
        dto.setIsInstructorOwner(isInstructorOwner);

        int totalLessons = course.getTotalLessons() != null ? course.getTotalLessons() : 0;
        int totalDuration = course.getTotalDuration() != null ? course.getTotalDuration() : 0;

        if (course.getSections() != null && !course.getSections().isEmpty()) {
            List<CourseDetailDTO.SectionDTO> sectionDTOs = course.getSections().stream().map(section -> {
                CourseDetailDTO.SectionDTO s = new CourseDetailDTO.SectionDTO();
                s.setId(section.getId());
                s.setTitle(section.getTitle());

                List<CourseDetailDTO.LessonDTO> lessonDTOs = new ArrayList<>();

                if (section.getLessons() != null) {
                    lessonDTOs = section.getLessons().stream()
                            .filter(java.util.Objects::nonNull)
                            .map(l -> {
                                CourseDetailDTO.LessonDTO ld = new CourseDetailDTO.LessonDTO();
                                ld.setId(l.getId());
                                ld.setTitle(l.getTitle());
                                ld.setType(l.getType() != null ? l.getType().name() : "VIDEO");
                                ld.setDuration(l.getDuration() != null ? l.getDuration() : 0);
                                ld.setIsPreview(l.getIsPreview() != null ? l.getIsPreview() : false);
                                ld.setIsCompleted(false);
                                ld.setVideoUrl(l.getVideoUrl());
                                ld.setContentText(l.getContentText());
                                ld.setDocumentUrl(l.getDocumentUrl());

                                if (l.getQuiz() != null) {
                                    ld.setQuizId(l.getQuiz().getId());
                                    ld.setQuizPassingScore(l.getQuiz().getPassingScore());
                                }

                                return ld;
                            })
                            .collect(Collectors.toList());
                }

                s.setLessons(lessonDTOs);
                s.setLessonCount(lessonDTOs.size());

                int sectionDuration = lessonDTOs.stream()
                        .filter(ld -> "VIDEO".equalsIgnoreCase(ld.getType()))
                        .map(CourseDetailDTO.LessonDTO::getDuration)
                        .filter(java.util.Objects::nonNull)
                        .reduce(0, Integer::sum);

                s.setTotalDuration(sectionDuration);

                return s;
            }).collect(Collectors.toList());

            dto.setSections(sectionDTOs);
        } else {
            dto.setSections(new ArrayList<>());
        }

        dto.setTotalLessons(totalLessons);
        dto.setTotalDuration(totalDuration);

        // rental info
        boolean rentalEnabled = Boolean.TRUE.equals(course.getRentalEnabled());
        dto.setRentalEnabled(rentalEnabled);
        if (rentalEnabled && course.getPrice() != null && course.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
            dto.setRentalPrice7d(course.getPrice().multiply(java.math.BigDecimal.valueOf(0.15)).setScale(0, java.math.RoundingMode.HALF_UP));
            dto.setRentalPrice30d(course.getPrice().multiply(java.math.BigDecimal.valueOf(0.30)).setScale(0, java.math.RoundingMode.HALF_UP));
            dto.setRentalPrice90d(course.getPrice().multiply(java.math.BigDecimal.valueOf(0.50)).setScale(0, java.math.RoundingMode.HALF_UP));
        }

        return dto;
    }

    private boolean hasCourseOwnership(String currentEmail, Long courseId) {
        if (currentEmail == null || currentEmail.isBlank() || courseId == null) {
            return false;
        }

        // BLOCKED = access revoked due to refund — always not owned, regardless of order status
        if (enrollmentRepository.existsByStudent_EmailAndCourse_IdAndStatus(currentEmail, courseId, "BLOCKED")) {
            return false;
        }

        boolean hasActiveEnrollment = enrollmentRepository.existsByStudent_EmailAndCourse_IdAndStatus(currentEmail, courseId, "ACTIVE");
        if (hasActiveEnrollment) {
            return true;
        }

        return studentRepository.findByEmail(currentEmail)
                .map(student -> orderRepository.existsByStudentIdAndCourseIdAndStatus(
                        student.getId(),
                        courseId,
                        org.example.multileanproject.entity.OrderStatus.COMPLETED
                ))
                .orElse(false);
    }

    private boolean isInstructorOwner(Course course, String email) {
        return course.getInstructor() != null
                && course.getInstructor().getUser() != null
                && course.getInstructor().getUser().getEmail() != null
                && email != null
                && email.equalsIgnoreCase(course.getInstructor().getUser().getEmail());
    }

    private boolean hasBasicInfoChange(Course course, CourseRequestDTO req) {
        if (req == null) return false;
        if (req.getTitle() != null && !req.getTitle().equals(course.getTitle())) return true;
        if (req.getShortDescription() != null && !req.getShortDescription().equals(course.getShortDescription())) return true;
        if (req.getDescription() != null && !req.getDescription().equals(course.getDescription())) return true;
        if (req.getLearningOutcomes() != null && !req.getLearningOutcomes().equals(course.getLearningOutcomes())) return true;
        if (req.getLanguage() != null && !req.getLanguage().equals(course.getLanguage())) return true;
        if (req.getLevel() != null && !req.getLevel().equals(course.getLevel())) return true;
        if (req.getThumbnail() != null && !req.getThumbnail().equals(course.getThumbnail())) return true;
        if (req.getCategoryId() != null) {
            Long currentCatId = course.getCategory() != null ? course.getCategory().getId() : null;
            if (!req.getCategoryId().equals(currentCatId)) return true;
        }
        return false;
    }

    private boolean hasMajorContentChange(Course currentCourse, CourseRequestDTO request) {
        if (currentCourse == null || request == null) {
            return false;
        }

        if (!normalizeText(currentCourse.getTitle()).equals(normalizeText(request.getTitle()))) {
            return true;
        }
        if (!normalizeText(currentCourse.getShortDescription()).equals(normalizeText(request.getShortDescription()))) {
            return true;
        }
        if (!normalizeText(currentCourse.getDescription()).equals(normalizeText(request.getDescription()))) {
            return true;
        }
        if (!normalizeText(currentCourse.getLearningOutcomes()).equals(normalizeText(request.getLearningOutcomes()))) {
            return true;
        }

        Long currentCategoryId = currentCourse.getCategory() != null ? currentCourse.getCategory().getId() : null;
        if (!Objects.equals(currentCategoryId, request.getCategoryId())) {
            return true;
        }

        if (request.getSections() == null) {
            return false;
        }

        String beforeFingerprint = buildSectionFingerprint(currentCourse.getSections());
        String afterFingerprint = buildSectionRequestFingerprint(request.getSections());
        return !Objects.equals(beforeFingerprint, afterFingerprint);
    }

    private void ensureNoHardDeleteOnEnrolledCourse(Course existingCourse, CourseRequestDTO request) {
        if (existingCourse == null || existingCourse.getId() == null || request == null || request.getSections() == null) {
            return;
        }

        long enrolledCount = enrollmentRepository.countByCourse_Id(existingCourse.getId());
        if (enrolledCount <= 0) {
            return;
        }

        Set<Long> incomingSectionIds = request.getSections().stream()
                .filter(Objects::nonNull)
                .map(SectionRequestDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, SectionRequestDTO> incomingSectionMap = request.getSections().stream()
                .filter(Objects::nonNull)
                .filter(sec -> sec.getId() != null)
                .collect(Collectors.toMap(SectionRequestDTO::getId, sec -> sec, (a, b) -> a));

        List<Section> existingSections = existingCourse.getSections() != null ? existingCourse.getSections() : List.of();
        for (Section section : existingSections) {
            if (section == null || section.getId() == null) {
                continue;
            }

            if (!incomingSectionIds.contains(section.getId())) {
                logInstructorCommercialAction(
                        existingCourse,
                        "BLOCK_HARD_DELETE_SECTION",
                        "Chan xoa section tren khoa hoc da co hoc vien: sectionId=" + section.getId()
                );
                throw new RuntimeException(
                        "Khoa hoc da co hoc vien. Khong duoc xoa section \"" + normalizeText(section.getTitle())
                                + "\" (ID=" + section.getId() + "). Hay danh dau an/noi dung thay the."
                );
            }

            SectionRequestDTO incomingSection = incomingSectionMap.get(section.getId());
            Set<Long> incomingLessonIds = incomingSection == null || incomingSection.getLessons() == null
                    ? Set.of()
                    : incomingSection.getLessons().stream()
                    .filter(Objects::nonNull)
                    .map(LessonRequestDTO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<Lesson> existingLessons = section.getLessons() != null ? section.getLessons() : List.of();
            for (Lesson lesson : existingLessons) {
                if (lesson == null || lesson.getId() == null) {
                    continue;
                }
                if (!incomingLessonIds.contains(lesson.getId())) {
                    logInstructorCommercialAction(
                            existingCourse,
                            "BLOCK_HARD_DELETE_LESSON",
                            "Chan xoa lesson tren khoa hoc da co hoc vien: lessonId=" + lesson.getId()
                    );
                    throw new RuntimeException(
                            "Khoa hoc da co hoc vien. Khong duoc xoa lesson \"" + normalizeText(lesson.getTitle())
                                    + "\" (ID=" + lesson.getId() + "). Hay cap nhat noi dung thay vi xoa cung."
                    );
                }
            }
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private String buildSectionFingerprint(List<Section> sections) {
        if (sections == null || sections.isEmpty()) {
            return "";
        }

        return sections.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(s -> s.getOrderIndex() != null ? s.getOrderIndex() : 0))
                .map(section -> {
                    String sectionTitle = normalizeText(section.getTitle());
                    String lessonPart = section.getLessons() == null ? "" : section.getLessons().stream()
                            .filter(Objects::nonNull)
                            .sorted(Comparator.comparingInt(l -> l.getOrderIndex() != null ? l.getOrderIndex() : 0))
                            .map(this::buildLessonFingerprint)
                            .collect(Collectors.joining("||"));
                    return sectionTitle + ">>" + lessonPart;
                })
                .collect(Collectors.joining("##"));
    }

    private String buildSectionRequestFingerprint(List<SectionRequestDTO> sections) {
        if (sections == null || sections.isEmpty()) {
            return "";
        }

        return sections.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(s -> s.getOrderIndex() != null ? s.getOrderIndex() : 0))
                .map(section -> {
                    String sectionTitle = normalizeText(section.getTitle());
                    String lessonPart = section.getLessons() == null ? "" : section.getLessons().stream()
                            .filter(Objects::nonNull)
                            .sorted(Comparator.comparingInt(l -> l.getOrderIndex() != null ? l.getOrderIndex() : 0))
                            .map(this::buildLessonRequestFingerprint)
                            .collect(Collectors.joining("||"));
                    return sectionTitle + ">>" + lessonPart;
                })
                .collect(Collectors.joining("##"));
    }

    private String buildLessonFingerprint(Lesson lesson) {
        String type = lesson.getType() != null ? lesson.getType().name() : "";
        return String.join("|",
                normalizeText(lesson.getTitle()),
                type,
                String.valueOf(lesson.getDuration() != null ? lesson.getDuration() : 0),
                normalizeText(lesson.getVideoUrl()),
                normalizeText(lesson.getDocumentUrl()),
                normalizeText(lesson.getContentText()),
                String.valueOf(lesson.getQuiz() != null ? lesson.getQuiz().getId() : null),
                String.valueOf(lesson.getIsPreview() != null ? lesson.getIsPreview() : false)
        );
    }

    private String buildLessonRequestFingerprint(LessonRequestDTO lesson) {
        String type = lesson.getType() != null ? lesson.getType().trim().toUpperCase() : "";
        return String.join("|",
                normalizeText(lesson.getTitle()),
                type,
                String.valueOf(lesson.getDuration() != null ? lesson.getDuration() : 0),
                normalizeText(lesson.getVideoUrl()),
                normalizeText(lesson.getDocumentUrl()),
                normalizeText(lesson.getContentText()),
                String.valueOf(lesson.getQuizId()),
                String.valueOf(lesson.getIsPreview() != null ? lesson.getIsPreview() : false)
        );
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority())
                                || "ADMIN".equals(authority.getAuthority()));
    }

    private void logInstructorCommercialAction(Course course, String action, String description) {
        if (course == null || course.getId() == null) {
            return;
        }
        try {
            adminActionLogService.log(action, description, course.getId(), "COURSE");
        } catch (Exception ignored) {
        }
    }

    private Course getOwnedCourseOrAdmin(Long courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();

        boolean isAdmin = isAdmin(authentication);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học ID: " + courseId));

        if (isAdmin) {
            return course;
        }

        if (course.getInstructor() == null
                || course.getInstructor().getUser() == null
                || course.getInstructor().getUser().getEmail() == null
                || !currentEmail.equalsIgnoreCase(course.getInstructor().getUser().getEmail())) {
            throw new RuntimeException("Bạn không có quyền thao tác với khóa học này.");
        }

        return course;
    }
}


