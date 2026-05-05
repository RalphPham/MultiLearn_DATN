package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.QuizCreateRequestDTO;
import org.example.multileanproject.dto.QuizDetailResponseDTO;
import org.example.multileanproject.dto.QuizSubmissionRequest;
import org.example.multileanproject.dto.QuizSubmissionResponse;
import org.example.multileanproject.dto.UserAnswer;
import org.example.multileanproject.entity.Answer;
import org.example.multileanproject.entity.Enrollment;
import org.example.multileanproject.entity.Question;
import org.example.multileanproject.entity.Quiz;
import org.example.multileanproject.entity.QuizSubmission;
import org.example.multileanproject.entity.Section;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.repository.AnswerRepository;
import org.example.multileanproject.repository.EnrollmentRepository;
import org.example.multileanproject.repository.QuestionRepository;
import org.example.multileanproject.repository.QuizRepository;
import org.example.multileanproject.repository.QuizSubmissionRepository;
import org.example.multileanproject.repository.SectionRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuizSubmissionRepository submissionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;

    private void assertSectionOwnership(Section section) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equals(a.getAuthority()));
        if (isAdmin) return;
        String ownerEmail = section.getCourse().getInstructor().getUser().getEmail();
        if (!auth.getName().equalsIgnoreCase(ownerEmail)) {
            throw new RuntimeException("Bạn không có quyền tạo quiz cho section này.");
        }
    }

    @Override
    public Quiz createQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    @Override
    @Transactional
    public QuizDetailResponseDTO createQuizWithQuestions(QuizCreateRequestDTO request) {
        if (request == null) {
            throw new RuntimeException("Dữ liệu tạo quiz không hợp lệ.");
        }

        if (request.getSectionId() == null) {
            throw new RuntimeException("Thiếu sectionId để tạo quiz.");
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Tiêu đề quiz không được để trống.");
        }

        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new RuntimeException("Quiz phải có ít nhất 1 câu hỏi.");
        }

        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy section với ID: " + request.getSectionId()));

        assertSectionOwnership(section);

        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle().trim());
        quiz.setPassingScore(request.getPassingScore() != null ? request.getPassingScore() : 70);
        quiz.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 1);
        quiz.setSection(section);

        Quiz savedQuiz = quizRepository.save(quiz);

        for (QuizCreateRequestDTO.QuestionCreateRequestDTO qDto : request.getQuestions()) {
            if (qDto.getContent() == null || qDto.getContent().trim().isEmpty()) {
                throw new RuntimeException("Nội dung câu hỏi không được để trống.");
            }

            if (qDto.getAnswers() == null || qDto.getAnswers().size() < 2) {
                throw new RuntimeException("Mỗi câu hỏi phải có ít nhất 2 đáp án.");
            }

            Question question = new Question();
            question.setQuiz(savedQuiz);
            question.setContent(qDto.getContent().trim());
            question.setScore(qDto.getScore() != null ? qDto.getScore() : 1.0);

            Question savedQuestion = questionRepository.save(question);

            boolean hasCorrect = false;

            for (QuizCreateRequestDTO.AnswerCreateRequestDTO aDto : qDto.getAnswers()) {
                if (aDto.getContent() == null || aDto.getContent().trim().isEmpty()) {
                    throw new RuntimeException("Nội dung đáp án không được để trống.");
                }

                Answer answer = new Answer();
                answer.setQuestion(savedQuestion);
                answer.setContent(aDto.getContent().trim());
                answer.setIsCorrect(Boolean.TRUE.equals(aDto.getIsCorrect()));

                if (Boolean.TRUE.equals(answer.getIsCorrect())) {
                    hasCorrect = true;
                }

                answerRepository.save(answer);
            }

            if (!hasCorrect) {
                throw new RuntimeException("Mỗi câu hỏi phải có ít nhất 1 đáp án đúng.");
            }
        }

        return buildQuizDetailResponse(savedQuiz.getId());
    }

    @Override
    @Transactional
    public QuizDetailResponseDTO updateQuizWithQuestions(Long quizId, QuizCreateRequestDTO request) {
        if (quizId == null) {
            throw new RuntimeException("Thiếu quizId để cập nhật quiz.");
        }

        if (request == null) {
            throw new RuntimeException("Dữ liệu cập nhật quiz không hợp lệ.");
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Tiêu đề quiz không được để trống.");
        }

        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new RuntimeException("Quiz phải có ít nhất 1 câu hỏi.");
        }

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quiz ID: " + quizId));

        if (quiz.getSection() == null) {
            throw new RuntimeException("Quiz không có section hợp lệ.");
        }

        assertSectionOwnership(quiz.getSection());

        quiz.setTitle(request.getTitle().trim());
        quiz.setPassingScore(request.getPassingScore() != null ? request.getPassingScore() : 70);
        quiz.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : quiz.getOrderIndex());
        quiz.getQuestions().clear();

        for (QuizCreateRequestDTO.QuestionCreateRequestDTO qDto : request.getQuestions()) {
            if (qDto.getContent() == null || qDto.getContent().trim().isEmpty()) {
                throw new RuntimeException("Nội dung câu hỏi không được để trống.");
            }

            if (qDto.getAnswers() == null || qDto.getAnswers().size() < 2) {
                throw new RuntimeException("Mỗi câu hỏi phải có ít nhất 2 đáp án.");
            }

            Question question = new Question();
            question.setQuiz(quiz);
            question.setContent(qDto.getContent().trim());
            question.setScore(qDto.getScore() != null ? qDto.getScore() : 1.0);

            boolean hasCorrect = false;

            for (QuizCreateRequestDTO.AnswerCreateRequestDTO aDto : qDto.getAnswers()) {
                if (aDto.getContent() == null || aDto.getContent().trim().isEmpty()) {
                    throw new RuntimeException("Nội dung đáp án không được để trống.");
                }

                Answer answer = new Answer();
                answer.setQuestion(question);
                answer.setContent(aDto.getContent().trim());
                answer.setIsCorrect(Boolean.TRUE.equals(aDto.getIsCorrect()));

                if (Boolean.TRUE.equals(answer.getIsCorrect())) {
                    hasCorrect = true;
                }

                question.getAnswers().add(answer);
            }

            if (!hasCorrect) {
                throw new RuntimeException("Mỗi câu hỏi phải có ít nhất 1 đáp án đúng.");
            }

            quiz.getQuestions().add(question);
        }

        Quiz savedQuiz = quizRepository.saveAndFlush(quiz);
        return buildQuizDetailResponse(savedQuiz.getId());
    }

    @Override
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDetailResponseDTO getQuizDetail(Long quizId) {
        return buildQuizDetailResponse(quizId);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDetailResponseDTO getQuizDetailForStudent(Long quizId) {
        return buildStudentQuizDetailResponse(quizId);
    }

    @Override
    public double submitQuiz(Long quizId, List<UserAnswer> answers) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Bài kiểm tra không tồn tại."));

        List<Question> dbQuestions = questionRepository.findByQuiz_Id(quiz.getId());
        if (dbQuestions.isEmpty()) {
            throw new RuntimeException("Bài kiểm tra chưa có câu hỏi nào được thiết lập.");
        }

        List<Long> questionIds = dbQuestions.stream()
                .map(Question::getId)
                .toList();

        List<Answer> dbAnswers = answerRepository.findByQuestion_IdIn(questionIds);

        double maxTotalScore = 0.0;
        double userEarnedScore = 0.0;

        List<UserAnswer> submittedAnswers = answers != null ? answers : List.of();

        for (Question q : dbQuestions) {
            double questionWeight = q.getScore() != null ? q.getScore() : 1.0;
            maxTotalScore += questionWeight;

            Optional<UserAnswer> studentAnswerOpt = submittedAnswers.stream()
                    .filter(ans -> ans.getQuestionId().equals(q.getId()))
                    .findFirst();

            if (studentAnswerOpt.isPresent()) {
                Long selectedOptionId = studentAnswerOpt.get().getSelectedOptionId();

                boolean isCorrect = dbAnswers.stream()
                        .anyMatch(a -> a.getId().equals(selectedOptionId)
                                && a.getQuestion() != null
                                && a.getQuestion().getId().equals(q.getId())
                                && Boolean.TRUE.equals(a.getIsCorrect()));

                if (isCorrect) {
                    userEarnedScore += questionWeight;
                }
            }
        }

        if (maxTotalScore <= 0) {
            return 0.0;
        }

        return BigDecimal.valueOf((userEarnedScore / maxTotalScore) * 100.0)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    @Transactional
    public QuizSubmissionResponse submitQuiz(QuizSubmissionRequest request, String email) {
        if (request == null || request.getQuizId() == null) {
            throw new RuntimeException("Thiếu quizId khi nộp bài.");
        }

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin sinh viên từ email: " + email));

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new RuntimeException("Bài kiểm tra không tồn tại."));

        if (quiz.getSection() == null || quiz.getSection().getCourse() == null) {
            throw new RuntimeException("Dữ liệu cấu trúc bài học bị lỗi (thiếu Course).");
        }

        Long courseId = quiz.getSection().getCourse().getId();

        Enrollment enrollment = enrollmentRepository
                .findByStudent_IdAndCourse_Id(student.getId(), courseId)
                .orElseThrow(() -> new RuntimeException("Bạn chưa tham gia khóa học này, không thể nộp bài!"));


        List<Question> dbQuestions = questionRepository.findByQuiz_Id(quiz.getId());
        if (dbQuestions.isEmpty()) {
            throw new RuntimeException("Bài kiểm tra chưa có câu hỏi nào được thiết lập.");
        }

        List<Long> questionIds = dbQuestions.stream()
                .map(Question::getId)
                .toList();

        List<Answer> dbAnswers = answerRepository.findByQuestion_IdIn(questionIds);

        double maxTotalScore = 0.0;
        double userEarnedScore = 0.0;
        int correctCount = 0;

        List<QuizSubmissionRequest.AnswerRequest> submittedAnswers =
                request.getAnswers() != null ? request.getAnswers() : List.of();

        for (Question q : dbQuestions) {
            double questionWeight = q.getScore() != null ? q.getScore() : 1.0;
            maxTotalScore += questionWeight;

            Optional<QuizSubmissionRequest.AnswerRequest> studentAnswerOpt = submittedAnswers.stream()
                    .filter(ans -> ans.getQuestionId().equals(q.getId()))
                    .findFirst();

            if (studentAnswerOpt.isPresent()) {
                Long selectedOptionId = studentAnswerOpt.get().getSelectedOptionId();

                boolean isCorrect = dbAnswers.stream()
                        .anyMatch(a -> a.getId().equals(selectedOptionId)
                                && a.getQuestion() != null
                                && a.getQuestion().getId().equals(q.getId())
                                && Boolean.TRUE.equals(a.getIsCorrect()));

                if (isCorrect) {
                    userEarnedScore += questionWeight;
                    correctCount++;
                }
            }
        }

        BigDecimal finalScore = BigDecimal.ZERO;
        if (maxTotalScore > 0) {
            double percentage = (userEarnedScore / maxTotalScore) * 100.0;
            finalScore = BigDecimal.valueOf(percentage).setScale(2, RoundingMode.HALF_UP);
        }

        int passingScore = quiz.getPassingScore() != null ? quiz.getPassingScore() : 50;
        boolean isPassed = finalScore.doubleValue() >= passingScore;

        QuizSubmission submission = new QuizSubmission();
        submission.setEnrollmentId(enrollment.getId());
        submission.setQuizId(quiz.getId());
        submission.setScore(finalScore);
        submission.setIsPassed(isPassed);
        submission.setSubmittedAt(LocalDateTime.now());

        submissionRepository.save(submission);

        return QuizSubmissionResponse.builder()
                .score(finalScore)
                .isPassed(isPassed)
                .correctCount(correctCount)
                .totalQuestions(dbQuestions.size())
                .message(isPassed
                        ? "Chúc mừng! Bạn đã hoàn thành xuất sắc bài kiểm tra."
                        : "Rất tiếc! Bạn cần cố gắng hơn để đạt điểm qua môn.")
                .build();
    }

    private QuizDetailResponseDTO buildQuizDetailResponse(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài kiểm tra này."));

        List<Question> questions = questionRepository.findByQuiz_Id(quizId);

        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .toList();

        List<Answer> allAnswers = questionIds.isEmpty()
                ? List.of()
                : answerRepository.findByQuestion_IdIn(questionIds);

        Map<Long, List<Answer>> answersByQuestionId = allAnswers.stream()
                .filter(a -> a.getQuestion() != null && a.getQuestion().getId() != null)
                .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));

        return QuizDetailResponseDTO.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .passingScore(quiz.getPassingScore())
                .orderIndex(quiz.getOrderIndex())
                .sectionId(quiz.getSection() != null ? quiz.getSection().getId() : null)
                .questions(
                        questions.stream()
                                .sorted(Comparator.comparing(Question::getId))
                                .map(question -> QuizDetailResponseDTO.QuestionDTO.builder()
                                        .id(question.getId())
                                        .content(question.getContent())
                                        .score(question.getScore())
                                        .answers(
                                                answersByQuestionId.getOrDefault(question.getId(), List.of())
                                                        .stream()
                                                        .sorted(Comparator.comparing(Answer::getId))
                                                        .map(answer -> QuizDetailResponseDTO.AnswerDTO.builder()
                                                                .id(answer.getId())
                                                                .content(answer.getContent())
                                                                .isCorrect(Boolean.TRUE.equals(answer.getIsCorrect()))
                                                                .build())
                                                        .toList()
                                        )
                                        .build())
                                .toList()
                )
                .build();
    }

    // Phiên bản dành cho học viên: ẩn isCorrect để tránh gian lận
    private QuizDetailResponseDTO buildStudentQuizDetailResponse(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài kiểm tra này."));

        List<Question> questions = questionRepository.findByQuiz_Id(quizId);
        List<Long> questionIds = questions.stream().map(Question::getId).toList();

        List<Answer> allAnswers = questionIds.isEmpty()
                ? List.of()
                : answerRepository.findByQuestion_IdIn(questionIds);

        Map<Long, List<Answer>> answersByQuestionId = allAnswers.stream()
                .filter(a -> a.getQuestion() != null && a.getQuestion().getId() != null)
                .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));

        return QuizDetailResponseDTO.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .passingScore(quiz.getPassingScore())
                .orderIndex(quiz.getOrderIndex())
                .sectionId(quiz.getSection() != null ? quiz.getSection().getId() : null)
                .questions(
                        questions.stream()
                                .sorted(Comparator.comparing(Question::getId))
                                .map(question -> QuizDetailResponseDTO.QuestionDTO.builder()
                                        .id(question.getId())
                                        .content(question.getContent())
                                        .score(question.getScore())
                                        .answers(
                                                answersByQuestionId.getOrDefault(question.getId(), List.of())
                                                        .stream()
                                                        .sorted(Comparator.comparing(Answer::getId))
                                                        .map(answer -> QuizDetailResponseDTO.AnswerDTO.builder()
                                                                .id(answer.getId())
                                                                .content(answer.getContent())
                                                                .isCorrect(null) // ẨN đáp án đúng với học viên
                                                                .build())
                                                        .toList()
                                        )
                                        .build())
                                .toList()
                )
                .build();
    }
}
