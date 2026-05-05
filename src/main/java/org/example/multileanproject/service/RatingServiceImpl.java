package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.RatingEligibilityResponseDTO;
import org.example.multileanproject.dto.RatingRequestDTO;
import org.example.multileanproject.dto.RatingResponseDTO;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.Enrollment;
import org.example.multileanproject.entity.Notification;
import org.example.multileanproject.entity.Rating;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.EnrollmentRepository;
import org.example.multileanproject.repository.RatingRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RatingServiceImpl implements RatingService {

    private static final Logger log = LoggerFactory.getLogger(RatingServiceImpl.class);

    private final RatingRepository ratingRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationService notificationService;

    // 🔥 ĐÃ ĐỔI ĐIỀU KIỆN TỪ 100% XUỐNG 80%
    @Override
    public RatingResponseDTO createRating(Long courseId, RatingRequestDTO request) {
        validateStars(request.getStars());

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));

        Student student = getCurrentStudent();

        Enrollment enrollment = enrollmentRepository.findByStudent_IdAndCourse_Id(student.getId(), courseId)
                .orElseThrow(() -> new RuntimeException("Bạn chưa đăng ký khóa học này nên không thể đánh giá"));

        double progress = enrollment.getProgress() == null ? 0.0 : enrollment.getProgress().doubleValue();
        if (progress < 80.0) { // <--- SỬA 100 THÀNH 80 Ở ĐÂY
            throw new RuntimeException("Bạn phải hoàn thành tối thiểu 80% khóa học mới có thể đánh giá");
        }

        boolean existed = ratingRepository.existsByCourse_IdAndStudent_Id(courseId, student.getId());
        if (existed) {
            throw new RuntimeException("Bạn đã đánh giá khóa học này rồi. Vui lòng sử dụng tính năng cập nhật.");
        }

        String normalizedComment = normalizeComment(request.getComment());

        Rating rating = Rating.builder()
                .stars(request.getStars())
                .comment(normalizedComment)
                .student(student)
                .course(course)
                .build();

        Rating saved = ratingRepository.save(rating);

        updateCourseAverageRating(course);

        if (course.getInstructor() != null && course.getInstructor().getUser() != null) {
            notificationService.createNotification(
                    course.getInstructor().getUser().getId(),
                    "Đánh giá mới: " + request.getStars() + " sao ⭐",
                    "Học viên " + student.getFullName() + " vừa để lại đánh giá cho khóa học của bạn.",
                    "/instructor/performance/reviews",
                    "INSTRUCTOR_NEW_RATING",
                    Notification.NotificationCategory.INSTRUCTOR_ANNOUNCEMENT,
                    false,
                    course.getTitle(),
                    student.getFullName()
            );
        }

        return mapToResponse(saved);
    }

    // 🔥 THÊM MỚI: API ĐỂ CẬP NHẬT ĐÁNH GIÁ (SỬA)
    @Override
    public RatingResponseDTO updateRating(Long courseId, RatingRequestDTO request) {
        validateStars(request.getStars());
        Student student = getCurrentStudent();

        Rating existingRating = ratingRepository.findByCourse_IdAndStudent_Id(courseId, student.getId())
                .orElseThrow(() -> new RuntimeException("Bạn chưa có đánh giá nào để cập nhật"));

        existingRating.setStars(request.getStars());
        existingRating.setComment(normalizeComment(request.getComment()));
        // Thời gian updatedAt sẽ được tự động cập nhật bởi Hibernate (@UpdateTimestamp) nếu entity đã cấu hình

        Rating saved = ratingRepository.save(existingRating);
        updateCourseAverageRating(existingRating.getCourse());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponseDTO> getRatingsByCourse(Long courseId) {
        return ratingRepository.findByCourse_IdOrderByCreatedAtDesc(courseId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long courseId) {
        Double avg = ratingRepository.getAverageRatingByCourseId(courseId);
        return avg == null ? 0.0 : round1(avg);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countRatings(Long courseId) {
        return ratingRepository.countByCourse_Id(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasStudentRatedCourse(Long courseId, Long studentId) {
        return ratingRepository.existsByCourse_IdAndStudent_Id(courseId, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponseDTO> getRatingsForInstructor(Long instructorId) {
        return ratingRepository.findByCourse_Instructor_IdOrderByCreatedAtDesc(instructorId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponseDTO> getRatingsForCurrentInstructor() {
        String email = getCurrentUserEmail();
        return ratingRepository.findRatingsByInstructorEmail(email)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 🔥 ĐÃ ĐỔI ĐIỀU KIỆN TỪ 100% XUỐNG 80%
    @Override
    @Transactional(readOnly = true)
    public RatingEligibilityResponseDTO getMyRatingEligibility(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return RatingEligibilityResponseDTO.builder()
                    .loggedIn(false)
                    .enrolled(false)
                    .completed(false)
                    .alreadyRated(false)
                    .eligible(false)
                    .courseId(courseId)
                    .message("Bạn cần đăng nhập để đánh giá khóa học")
                    .build();
        }

        Student student = getCurrentStudent();

        Enrollment enrollment = enrollmentRepository.findByStudent_IdAndCourse_Id(student.getId(), courseId)
                .orElse(null);

        if (enrollment == null) {
            return RatingEligibilityResponseDTO.builder()
                    .loggedIn(true)
                    .studentId(student.getId())
                    .enrolled(false)
                    .completed(false)
                    .alreadyRated(false)
                    .eligible(false)
                    .courseId(course.getId())
                    .message("Bạn cần mua khóa học này trước khi đánh giá")
                    .build();
        }

        double progress = enrollment.getProgress() == null ? 0.0 : enrollment.getProgress().doubleValue();
        boolean completed = progress >= 80.0; // <--- SỬA 100 THÀNH 80 Ở ĐÂY
        boolean alreadyRated = ratingRepository.existsByCourse_IdAndStudent_Id(courseId, student.getId());
        boolean eligible = completed; // Bỏ đi điều kiện && !alreadyRated vì đã đánh giá thì vẫn được quyền SỬA

        String message;
        if (!completed) {
            message = "Bạn phải hoàn thành tối thiểu 80% khóa học mới có thể đánh giá";
        } else if (alreadyRated) {
            message = "Bạn đã có đánh giá. Bạn có thể cập nhật lại.";
        } else {
            message = "Bạn có thể đánh giá khóa học này";
        }

        return RatingEligibilityResponseDTO.builder()
                .loggedIn(true)
                .studentId(student.getId())
                .enrolled(true)
                .completed(completed)
                .alreadyRated(alreadyRated)
                .eligible(eligible) // Trả về true kể cả khi đã rate để Frontend hiện nút "Sửa đánh giá"
                .courseId(course.getId())
                .progress(progress)
                .message(message)
                .build();
    }

    @Override
    public void deleteRating(Long courseId) {
        Student student = getCurrentStudent();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));

        Rating rating = ratingRepository.findByCourse_IdAndStudent_Id(courseId, student.getId())
                .orElseThrow(() -> new RuntimeException("Bạn chưa có đánh giá cho khóa học này"));

        ratingRepository.delete(rating);
        updateCourseAverageRating(course);
    }

    private Student getCurrentStudent() {
        String email = getCurrentUserEmail();
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin học viên từ tài khoản đăng nhập"));
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("Bạn chưa đăng nhập");
        }
        return authentication.getName();
    }

    private void validateStars(Integer stars) {
        if (stars == null || stars < 1 || stars > 5) {
            throw new RuntimeException("Số sao phải từ 1 đến 5");
        }
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }
        String trimmed = comment.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private void updateCourseAverageRating(Course course) {
        Double avg = ratingRepository.getAverageRatingByCourseId(course.getId());
        if (avg == null) {
            avg = 0.0;
        }

        try {
            course.setAverageRating(avg);
            courseRepository.save(course);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật rating trung bình cho course {}: {}", course.getId(), e.getMessage());
        }
    }

    private double round1(Double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private RatingResponseDTO mapToResponse(Rating rating) {
        String studentAvatar = null;
        try {
            studentAvatar = rating.getStudent().getAvatar();
        } catch (Exception ignored) {
        }

        return RatingResponseDTO.builder()
                .id(rating.getId())
                .stars(rating.getStars())
                .comment(rating.getComment())
                .studentId(rating.getStudent() != null ? rating.getStudent().getId() : null)
                .studentName(rating.getStudent() != null ? rating.getStudent().getFullName() : null)
                .studentAvatar(studentAvatar)
                .courseId(rating.getCourse() != null ? rating.getCourse().getId() : null)
                .courseTitle(rating.getCourse() != null ? rating.getCourse().getTitle() : null)
                .courseSlug(rating.getCourse() != null ? rating.getCourse().getSlug() : null)
                .courseThumbnail(rating.getCourse() != null ? rating.getCourse().getThumbnail() : null)
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}