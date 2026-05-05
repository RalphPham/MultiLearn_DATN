package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.QuizCreateRequestDTO;
import org.example.multileanproject.dto.QuizDetailResponseDTO;
import org.example.multileanproject.dto.QuizSubmissionRequest;
import org.example.multileanproject.dto.QuizSubmissionResponse;
import org.example.multileanproject.dto.UserAnswer;
import org.example.multileanproject.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    public ResponseEntity<QuizDetailResponseDTO> createQuiz(@RequestBody QuizCreateRequestDTO request) {
        return ResponseEntity.ok(quizService.createQuizWithQuestions(request));
    }

    // Dành cho giảng viên preview (có isCorrect)
    @PutMapping("/{quizId}")
    public ResponseEntity<QuizDetailResponseDTO> updateQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizCreateRequestDTO request
    ) {
        return ResponseEntity.ok(quizService.updateQuizWithQuestions(quizId, request));
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizDetailResponseDTO> getQuizDetail(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizDetail(quizId));
    }

    // Dành cho học viên làm bài (không có isCorrect)
    @GetMapping("/{quizId}/take")
    public ResponseEntity<QuizDetailResponseDTO> getQuizDetailForStudent(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizDetailForStudent(quizId));
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<QuizSubmissionResponse> submitQuizLegacy(
            @PathVariable Long quizId,
            @RequestBody List<UserAnswer> answers
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        QuizSubmissionRequest request = new QuizSubmissionRequest();
        request.setQuizId(quizId);

        List<QuizSubmissionRequest.AnswerRequest> convertedAnswers = answers.stream()
                .map(a -> {
                    QuizSubmissionRequest.AnswerRequest ar = new QuizSubmissionRequest.AnswerRequest();
                    ar.setQuestionId(a.getQuestionId());
                    ar.setSelectedOptionId(a.getSelectedOptionId());
                    return ar;
                })
                .toList();

        request.setAnswers(convertedAnswers);

        return ResponseEntity.ok(quizService.submitQuiz(request, email));
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizSubmissionResponse> submitQuiz(@RequestBody QuizSubmissionRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(quizService.submitQuiz(request, email));
    }
}
