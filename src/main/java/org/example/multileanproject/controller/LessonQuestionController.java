package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.AnswerRequestDTO;
import org.example.multileanproject.dto.QuestionDTO;
import org.example.multileanproject.dto.QuestionRequestDTO;
import org.example.multileanproject.service.LessonQuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class LessonQuestionController {

    private final LessonQuestionService questionService;

    // Học viên lấy danh sách câu hỏi của một bài học
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<QuestionDTO>> getLessonQuestions(
            @PathVariable Long lessonId,
            @RequestParam Long courseId) {
        return ResponseEntity.ok(questionService.getQuestionsByLesson(lessonId, courseId));
    }

    // Học viên đặt câu hỏi mới
    @PostMapping
    public ResponseEntity<QuestionDTO> createQuestion(@RequestBody QuestionRequestDTO req) {
        return ResponseEntity.ok(questionService.createQuestion(req));
    }

    // Giảng viên lấy toàn bộ câu hỏi thuộc các khóa học của mình
    @GetMapping("/instructor")
    public ResponseEntity<List<QuestionDTO>> getInstructorQuestions() {
        return ResponseEntity.ok(questionService.getQuestionsForInstructor());
    }

    // Giảng viên trả lời câu hỏi
    @PostMapping("/{questionId}/answer")
    public ResponseEntity<QuestionDTO> answerQuestion(
            @PathVariable Long questionId,
            @RequestBody AnswerRequestDTO req) {
        return ResponseEntity.ok(questionService.answerQuestion(questionId, req));
    }
}