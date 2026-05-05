package org.example.multileanproject.service;

import org.example.multileanproject.dto.CourseRequestDTO;
import org.example.multileanproject.entity.*;
import org.example.multileanproject.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplBasicInfoGuardTest {

    @Mock CourseRepository courseRepository;
    @Mock NotificationService notificationService;
    @Mock InstructorRepository instructorRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock OrderRepository orderRepository;
    @Mock EnrollmentRepository enrollmentRepository;
    @Mock QuizRepository quizRepository;
    @Mock LearningProgressRepository learningProgressRepository;
    @Mock LessonRepository lessonRepository;
    @Mock StudentRepository studentRepository;
    @Mock RefundRequestRepository refundRequestRepository;
    @Mock SaleCampaignItemRepository campaignItemRepository;
    @Mock AdminActionLogService adminActionLogService;

    @InjectMocks
    CourseServiceImpl courseService;

    @BeforeEach
    void setInstructorSecurityContext() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "instructor@test.com",
                        null,
                        List.of(new SimpleGrantedAuthority("INSTRUCTOR")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ============================================================
    // TEST-01: Instructor sửa basic info khóa PUBLISHED → tự động chuyển PENDING_APPROVAL
    // (Trước: throw 403; nay nới lỏng UX — auto-route qua flow duyệt lại)
    // ============================================================

    @Test
    void TEST01_instructor_editBasicInfo_publishedCourse_autoRoutesToPendingApproval() {
        Student student = Student.builder()
                .id(100L).email("instructor@test.com").fullName("GV A").build();
        Instructor instructor = Instructor.builder()
                .id(10L).user(student).build();
        Course course = Course.builder()
                .id(1L)
                .title("Java co ban")
                .status(CourseStatus.PUBLISHED)
                .instructor(instructor)
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseRequestDTO request = new CourseRequestDTO();
        request.setTitle("Java nang cao");

        assertThatCode(() -> courseService.updateCourse(1L, request))
                .doesNotThrowAnyException();
        assertThat(course.getStatus()).isEqualTo(CourseStatus.PENDING_APPROVAL);
    }

    // ============================================================
    // TEST-02: Instructor sửa basic info khi khoá đang INACTIVE_REQUESTED → vẫn 403
    // (Không cho xung đột với flow tạm dừng đang chờ duyệt)
    // ============================================================

    @Test
    void TEST02_instructor_editBasicInfo_inactiveRequestedCourse_stillThrows403() {
        Student student = Student.builder()
                .id(100L).email("instructor@test.com").fullName("GV A").build();
        Instructor instructor = Instructor.builder()
                .id(10L).user(student).build();
        Course course = Course.builder()
                .id(1L)
                .title("Java co ban")
                .status(CourseStatus.INACTIVE_REQUESTED)
                .instructor(instructor)
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        CourseRequestDTO request = new CourseRequestDTO();
        request.setTitle("Java nang cao");

        assertThatThrownBy(() -> courseService.updateCourse(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex ->
                        assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(403));
    }
}
