package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.AnswerRequestDTO;
import org.example.multileanproject.dto.QuestionDTO;
import org.example.multileanproject.dto.QuestionRequestDTO;
import org.example.multileanproject.entity.*;
import org.example.multileanproject.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonQuestionService {

    private final LessonQuestionRepository questionRepository;
    private final LessonAnswerRepository answerRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final EnrollmentService enrollmentService;
    private final NotificationService notificationService;

    public List<QuestionDTO> getQuestionsByLesson(Long lessonId, Long courseId) {
        Student currentStudent = getCurrentUser();

        if (currentStudent.getRole() == Role.STUDENT) {
            boolean isOwned = enrollmentService.isStudentEnrolled(currentStudent.getId(), courseId);
            if (!isOwned) throw new RuntimeException("Bạn phải tham gia khóa học mới được xem thảo luận.");
        }

        return questionRepository.findByLessonIdOrderByCreatedAtDesc(lessonId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public QuestionDTO createQuestion(QuestionRequestDTO req) {
        Student student = getCurrentUser();
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));

        if (student.getRole() == Role.STUDENT && !enrollmentService.isStudentEnrolled(student.getId(), course.getId())) {
            throw new RuntimeException("Bạn chưa sở hữu khóa học này.");
        }

        LessonQuestion q = LessonQuestion.builder()
                .lessonId(req.getLessonId())
                .course(course)
                .student(student)
                .content(req.getContent())
                .createdAt(LocalDateTime.now())
                .answerCount(0)
                .build();

        LessonQuestion savedQ = questionRepository.save(q);

        if (course.getInstructor() != null && course.getInstructor().getUser() != null) {
            notificationService.createNotification(
                    course.getInstructor().getUser().getId(),
                    "Có câu hỏi mới",
                    "Học viên " + student.getFullName() + " vừa đặt câu hỏi trong khóa " + course.getTitle(),
                    "/instructor/communication/messages",
                    "INSTRUCTOR_NEW_QUESTION",
                    Notification.NotificationCategory.INSTRUCTOR_ANNOUNCEMENT,
                    false,
                    course.getTitle(),
                    student.getFullName()
            );
        }

        return mapToDTO(savedQ);
    }

    public List<QuestionDTO> getQuestionsForInstructor() {
        Student user = getCurrentUser();
        if (user.getRole() != Role.INSTRUCTOR) {
            throw new RuntimeException("Chỉ giảng viên mới có quyền xem danh sách này.");
        }

        List<Long> myCourseIds = courseRepository.findByInstructorUserId(user.getId())
                .stream().map(Course::getId).collect(Collectors.toList());

        return questionRepository.findByCourseIdInOrderByCreatedAtDesc(myCourseIds)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public QuestionDTO answerQuestion(Long questionId, AnswerRequestDTO req) {
        Student user = getCurrentUser();
        LessonQuestion q = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        LessonAnswer.LessonAnswerBuilder ansBuilder = LessonAnswer.builder()
                .question(q)
                .content(req.getContent())
                .createdAt(LocalDateTime.now());

        if (user.getRole() == Role.INSTRUCTOR) {
            Instructor instructor = instructorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ giảng viên"));
            ansBuilder.instructor(instructor);
        } else {
            boolean isOwned = enrollmentService.isStudentEnrolled(user.getId(), q.getCourse().getId());
            if (!isOwned) throw new RuntimeException("Chỉ học viên trong khóa học mới được tham gia thảo luận.");
            ansBuilder.student(user);
        }

        answerRepository.save(ansBuilder.build());

        if (!user.getId().equals(q.getStudent().getId())) {
            String roleName = (user.getRole() == Role.INSTRUCTOR) ? "Giảng viên" : "Học viên";
            notificationService.createNotification(
                    q.getStudent().getId(),
                    "Câu hỏi của bạn đã được trả lời",
                    roleName + " " + user.getFullName() + " vừa trả lời câu hỏi của bạn.",
                    "/learning/course/" + q.getCourse().getId(),
                    "STUDENT_QA_REPLY",
                    Notification.NotificationCategory.SYSTEM_NOTIFICATION,
                    false,
                    q.getCourse().getTitle(),
                    user.getFullName()
            );
        }

        q.setAnswerCount(q.getAnswerCount() + 1);
        return mapToDTO(questionRepository.save(q));
    }

    private Student getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    // 🔥 MAP DỮ LIỆU CHUẨN ĐỂ FRONTEND NHẬN DIỆN GIẢNG VIÊN (Biến isInstructor)
    // 🔥 MAP DỮ LIỆU CHUẨN ĐỂ FRONTEND NHẬN DIỆN TÁC GIẢ KHÓA HỌC
    private QuestionDTO mapToDTO(LessonQuestion q) {
        List<QuestionDTO.AnswerDTO> answerDTOs = new java.util.ArrayList<>();

        // 1. Lấy User ID của Tác giả khóa học này
        Long courseAuthorUserId = null;
        if (q.getCourse() != null && q.getCourse().getInstructor() != null && q.getCourse().getInstructor().getUser() != null) {
            courseAuthorUserId = q.getCourse().getInstructor().getUser().getId();
        }

        final Long finalAuthorId = courseAuthorUserId;

        if (q.getAnswers() != null && !q.getAnswers().isEmpty()) {
            answerDTOs = q.getAnswers().stream().map(a -> {
                Long responderUserId = null;
                String responderName = "Học viên";

                // 2. Lấy ID của người trả lời (dù họ đang comment bằng profile Giảng viên hay Học viên)
                if (a.getInstructor() != null && a.getInstructor().getUser() != null) {
                    responderUserId = a.getInstructor().getUser().getId();
                    responderName = a.getInstructor().getFullName();
                } else if (a.getStudent() != null) {
                    responderUserId = a.getStudent().getId();
                    responderName = a.getStudent().getFullName();
                }

                // 3. SO SÁNH: Nếu ID người trả lời TRÙNG với ID Tác giả khóa học -> Chính là Giảng viên!
                boolean isCourseAuthor = (responderUserId != null && responderUserId.equals(finalAuthorId));

                return QuestionDTO.AnswerDTO.builder()
                        .id(a.getId())
                        .responderName(responderName)
                        .isInstructor(isCourseAuthor) // <--- Trả về true/false cực chuẩn cho Claude bắt
                        .content(a.getContent())
                        .createdAt(a.getCreatedAt())
                        .build();
            }).collect(Collectors.toList());
        }

        return QuestionDTO.builder()
                .id(q.getId())
                .lessonId(q.getLessonId())
                .studentName(q.getStudent() != null ? q.getStudent().getFullName() : "Học viên")
                .content(q.getContent())
                .createdAt(q.getCreatedAt())
                .answerCount(q.getAnswerCount())
                .courseName(q.getCourse() != null ? q.getCourse().getTitle() : "Khóa học")
                .answers(answerDTOs)
                .build();
    }
}