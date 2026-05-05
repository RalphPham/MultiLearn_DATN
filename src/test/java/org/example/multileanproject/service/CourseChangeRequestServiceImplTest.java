package org.example.multileanproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.multileanproject.dto.BasicInfoPayload;
import org.example.multileanproject.dto.CourseChangeRequestDTO;
import org.example.multileanproject.dto.CreateBasicInfoChangeRequestDTO;
import org.example.multileanproject.dto.ReviewCourseChangeRequestDTO;
import org.example.multileanproject.entity.*;
import org.example.multileanproject.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseChangeRequestServiceImplTest {

    @Mock CourseChangeRequestRepository changeRequestRepository;
    @Mock CourseRepository courseRepository;
    @Mock InstructorRepository instructorRepository;
    @Mock AdminRepository adminRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock AdminActionLogService adminActionLogService;
    @Mock NotificationService notificationService;
    @Spy  ObjectMapper objectMapper;

    @InjectMocks
    CourseChangeRequestServiceImpl service;

    private Student student;
    private Instructor instructor;
    private Course publishedCourse;
    private Course draftCourse;

    @BeforeEach
    void setUp() {
        student = Student.builder()
                .id(100L)
                .fullName("Nguyen Van A")
                .email("instructor@test.com")
                .build();

        instructor = Instructor.builder()
                .id(10L)
                .fullName("Nguyen Van A")
                .user(student)
                .build();

        publishedCourse = Course.builder()
                .id(1L)
                .title("Java co ban")
                .slug("java-co-ban")
                .status(CourseStatus.PUBLISHED)
                .instructor(instructor)
                .build();

        draftCourse = Course.builder()
                .id(2L)
                .title("Python co ban")
                .slug("python-co-ban")
                .status(CourseStatus.DRAFT)
                .instructor(instructor)
                .build();
    }

    private CreateBasicInfoChangeRequestDTO buildCreateDTO() {
        return CreateBasicInfoChangeRequestDTO.builder()
                .title("Java nang cao")
                .requestNote("Muon doi ten")
                .build();
    }

    private CourseChangeRequest pendingRequest() {
        return CourseChangeRequest.builder()
                .id(99L)
                .course(publishedCourse)
                .instructor(instructor)
                .status(CourseChangeRequestStatus.PENDING)
                .payloadBefore("{}")
                .payloadAfter("{\"title\":\"Java nang cao\"}")
                .build();
    }

    // ============================================================
    // TEST-02: Instructor tạo change request thành công
    // ============================================================

    @Test
    void TEST02_createRequest_onPublishedCourse_succeeds() {
        when(instructorRepository.findByUser_Email("instructor@test.com"))
                .thenReturn(Optional.of(instructor));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse));
        when(changeRequestRepository.findAllByCourse_IdAndStatus(1L, CourseChangeRequestStatus.PENDING))
                .thenReturn(List.of());
        when(changeRequestRepository.save(any())).thenAnswer(inv -> {
            CourseChangeRequest r = inv.getArgument(0);
            r.setId(99L);
            return r;
        });
        doNothing().when(adminActionLogService).log(any(), any(), any(), any());

        CourseChangeRequestDTO result = service.createBasicInfoRequest(1L, buildCreateDTO(), "instructor@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getCourseId()).isEqualTo(1L);
        verify(changeRequestRepository).save(any(CourseChangeRequest.class));
    }

    // ============================================================
    // TEST-03: Admin approve → dữ liệu course được cập nhật
    // ============================================================

    @Test
    void TEST03_approve_appliesPayloadAfterToCourse() throws Exception {
        BasicInfoPayload afterPayload = BasicInfoPayload.builder().title("Java nang cao").build();
        String afterJson = objectMapper.writeValueAsString(afterPayload);

        CourseChangeRequest request = CourseChangeRequest.builder()
                .id(99L).course(publishedCourse).instructor(instructor)
                .status(CourseChangeRequestStatus.PENDING)
                .payloadBefore("{}").payloadAfter(afterJson)
                .build();

        Admin admin = Admin.builder().id(1L).username("admin1").fullName("Admin").build();

        when(changeRequestRepository.findById(99L)).thenReturn(Optional.of(request));
        when(adminRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(changeRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(adminActionLogService).log(any(), any(), any(), any());

        service.approveRequest(99L, null, "admin1");

        assertThat(publishedCourse.getTitle()).isEqualTo("Java nang cao");
        verify(courseRepository).save(publishedCourse);
    }

    // ============================================================
    // TEST-04: Admin reject → course data KHÔNG thay đổi
    // ============================================================

    @Test
    void TEST04_reject_doesNotModifyCourse() {
        CourseChangeRequest request = pendingRequest();
        Admin admin = Admin.builder().id(1L).username("admin1").fullName("Admin").build();
        ReviewCourseChangeRequestDTO dto = ReviewCourseChangeRequestDTO.builder()
                .adminNote("Ten khong phu hop").build();

        when(changeRequestRepository.findById(99L)).thenReturn(Optional.of(request));
        when(adminRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));
        when(changeRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(adminActionLogService).log(any(), any(), any(), any());

        service.rejectRequest(99L, dto, "admin1");

        verify(courseRepository, never()).save(any());
        assertThat(request.getStatus()).isEqualTo(CourseChangeRequestStatus.REJECTED);
        assertThat(publishedCourse.getTitle()).isEqualTo("Java co ban");
    }

    // ============================================================
    // TEST-05: Tạo request mới → auto-cancel request PENDING cũ
    // ============================================================

    @Test
    void TEST05_createRequest_autoCancelsExistingPending() {
        CourseChangeRequest oldPending = pendingRequest();

        when(instructorRepository.findByUser_Email("instructor@test.com"))
                .thenReturn(Optional.of(instructor));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse));
        when(changeRequestRepository.findAllByCourse_IdAndStatus(1L, CourseChangeRequestStatus.PENDING))
                .thenReturn(List.of(oldPending));
        when(changeRequestRepository.saveAll(anyList())).thenReturn(List.of(oldPending));
        when(changeRequestRepository.save(any())).thenAnswer(inv -> {
            CourseChangeRequest r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });
        doNothing().when(adminActionLogService).log(any(), any(), any(), any());

        service.createBasicInfoRequest(1L, buildCreateDTO(), "instructor@test.com");

        assertThat(oldPending.getStatus()).isEqualTo(CourseChangeRequestStatus.CANCELED);
        verify(changeRequestRepository).saveAll(anyList());
    }

    // ============================================================
    // TEST-06: Tạo request trên khóa học DRAFT → 400
    // ============================================================

    @Test
    void TEST06_createRequest_draftCourse_throws400() {
        when(instructorRepository.findByUser_Email("instructor@test.com"))
                .thenReturn(Optional.of(instructor));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(draftCourse));

        assertThatThrownBy(() ->
                service.createBasicInfoRequest(2L, buildCreateDTO(), "instructor@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex ->
                        assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(400));
    }

    // ============================================================
    // TEST-07: Cancel request không PENDING → 400
    // ============================================================

    @Test
    void TEST07_cancelRequest_nonPendingStatus_throws400() {
        CourseChangeRequest approvedRequest = CourseChangeRequest.builder()
                .id(99L).course(publishedCourse).instructor(instructor)
                .status(CourseChangeRequestStatus.APPROVED)
                .payloadBefore("{}").payloadAfter("{}")
                .build();

        when(changeRequestRepository.findById(99L)).thenReturn(Optional.of(approvedRequest));

        assertThatThrownBy(() -> service.cancelRequest(99L, "instructor@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex ->
                        assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(400));
    }

    // ============================================================
    // TEST-08: Admin reject không có adminNote → 400
    // ============================================================

    @Test
    void TEST08_reject_blankAdminNote_throws400() {
        CourseChangeRequest request = pendingRequest();
        ReviewCourseChangeRequestDTO dto = ReviewCourseChangeRequestDTO.builder()
                .adminNote(null).build();

        when(changeRequestRepository.findById(99L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.rejectRequest(99L, dto, "admin1"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex ->
                        assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(400));
    }

    // ============================================================
    // TEST-09: Approve request đã xử lý → 400
    // ============================================================

    @Test
    void TEST09_approve_alreadyProcessedRequest_throws400() {
        CourseChangeRequest alreadyApproved = CourseChangeRequest.builder()
                .id(99L).course(publishedCourse).instructor(instructor)
                .status(CourseChangeRequestStatus.APPROVED)
                .payloadBefore("{}").payloadAfter("{}")
                .build();

        when(changeRequestRepository.findById(99L)).thenReturn(Optional.of(alreadyApproved));

        assertThatThrownBy(() -> service.approveRequest(99L, null, "admin1"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex ->
                        assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(400));
    }

    // ============================================================
    // TEST-10: createBasicInfoRequest ghi log REQUEST_BASIC_INFO_EDIT
    // ============================================================

    @Test
    void TEST10_createRequest_logsRequestBasicInfoEdit() {
        when(instructorRepository.findByUser_Email("instructor@test.com"))
                .thenReturn(Optional.of(instructor));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(publishedCourse));
        when(changeRequestRepository.findAllByCourse_IdAndStatus(1L, CourseChangeRequestStatus.PENDING))
                .thenReturn(List.of());
        when(changeRequestRepository.save(any())).thenAnswer(inv -> {
            CourseChangeRequest r = inv.getArgument(0);
            r.setId(99L);
            return r;
        });

        service.createBasicInfoRequest(1L, buildCreateDTO(), "instructor@test.com");

        verify(adminActionLogService).log(
                eq("REQUEST_BASIC_INFO_EDIT"),
                any(),
                any(),
                eq("COURSE_CHANGE_REQUEST"));
    }

    // ============================================================
    // TEST-11: approveRequest ghi log APPROVE_BASIC_INFO_EDIT
    // ============================================================

    @Test
    void TEST11_approve_logsApproveBasicInfoEdit() throws Exception {
        String afterJson = objectMapper.writeValueAsString(
                BasicInfoPayload.builder().title("New Title").build());

        CourseChangeRequest request = CourseChangeRequest.builder()
                .id(99L).course(publishedCourse).instructor(instructor)
                .status(CourseChangeRequestStatus.PENDING)
                .payloadBefore("{}").payloadAfter(afterJson).build();

        Admin admin = Admin.builder().id(1L).username("admin1").fullName("Admin").build();

        when(changeRequestRepository.findById(99L)).thenReturn(Optional.of(request));
        when(adminRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(changeRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approveRequest(99L, null, "admin1");

        verify(adminActionLogService).log(
                eq("APPROVE_BASIC_INFO_EDIT"),
                any(),
                any(),
                eq("COURSE_CHANGE_REQUEST"));
    }

    // ============================================================
    // TEST-12: rejectRequest ghi log REJECT_BASIC_INFO_EDIT
    // ============================================================

    @Test
    void TEST12_reject_logsRejectBasicInfoEdit() {
        CourseChangeRequest request = pendingRequest();
        Admin admin = Admin.builder().id(1L).username("admin1").fullName("Admin").build();
        ReviewCourseChangeRequestDTO dto = ReviewCourseChangeRequestDTO.builder()
                .adminNote("Ly do").build();

        when(changeRequestRepository.findById(99L)).thenReturn(Optional.of(request));
        when(adminRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));
        when(changeRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.rejectRequest(99L, dto, "admin1");

        verify(adminActionLogService).log(
                eq("REJECT_BASIC_INFO_EDIT"),
                any(),
                any(),
                eq("COURSE_CHANGE_REQUEST"));
    }

    // ============================================================
    // TEST-13: Approve → gửi notification type INSTRUCTOR_BASIC_INFO_APPROVED
    // ============================================================

    @Test
    void TEST13_approve_sendsApprovedNotificationToInstructor() throws Exception {
        String afterJson = objectMapper.writeValueAsString(
                BasicInfoPayload.builder().title("New Title").build());

        CourseChangeRequest request = CourseChangeRequest.builder()
                .id(99L).course(publishedCourse).instructor(instructor)
                .status(CourseChangeRequestStatus.PENDING)
                .payloadBefore("{}").payloadAfter(afterJson).build();

        Admin admin = Admin.builder().id(1L).username("admin1").fullName("Admin").build();

        when(changeRequestRepository.findById(99L)).thenReturn(Optional.of(request));
        when(adminRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(changeRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(adminActionLogService).log(any(), any(), any(), any());

        service.approveRequest(99L, null, "admin1");

        verify(notificationService).createNotification(
                eq(student.getId()),
                any(), any(), any(),
                eq("INSTRUCTOR_BASIC_INFO_APPROVED"),
                any(), anyBoolean(), any(), any());
    }

    // ============================================================
    // TEST-14: Reject → gửi notification type INSTRUCTOR_BASIC_INFO_REJECTED
    // ============================================================

    @Test
    void TEST14_reject_sendsRejectedNotificationToInstructor() {
        CourseChangeRequest request = pendingRequest();
        Admin admin = Admin.builder().id(1L).username("admin1").fullName("Admin").build();
        ReviewCourseChangeRequestDTO dto = ReviewCourseChangeRequestDTO.builder()
                .adminNote("Khong phu hop").build();

        when(changeRequestRepository.findById(99L)).thenReturn(Optional.of(request));
        when(adminRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));
        when(changeRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(adminActionLogService).log(any(), any(), any(), any());

        service.rejectRequest(99L, dto, "admin1");

        verify(notificationService).createNotification(
                eq(student.getId()),
                any(), any(), any(),
                eq("INSTRUCTOR_BASIC_INFO_REJECTED"),
                any(), anyBoolean(), any(), any());
    }
}
