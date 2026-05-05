package org.example.multileanproject.service;

import org.example.multileanproject.dto.QuizCreateRequestDTO;
import org.example.multileanproject.dto.QuizDetailResponseDTO;
import org.example.multileanproject.dto.QuizSubmissionRequest;
import org.example.multileanproject.dto.QuizSubmissionResponse;
import org.example.multileanproject.dto.UserAnswer;
import org.example.multileanproject.entity.Quiz;

import java.util.List;

public interface QuizService {
    Quiz createQuiz(Quiz quiz);

    QuizDetailResponseDTO createQuizWithQuestions(QuizCreateRequestDTO request);

    QuizDetailResponseDTO updateQuizWithQuestions(Long quizId, QuizCreateRequestDTO request);

    List<Quiz> getAllQuizzes();

    QuizDetailResponseDTO getQuizDetail(Long quizId);

    // Dành cho học viên làm bài — không có trường isCorrect
    QuizDetailResponseDTO getQuizDetailForStudent(Long quizId);

    double submitQuiz(Long quizId, List<UserAnswer> answers);

    QuizSubmissionResponse submitQuiz(QuizSubmissionRequest request, String email);
}
