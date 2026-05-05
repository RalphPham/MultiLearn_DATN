package org.example.multileanproject.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class QuestionDTO {
    private Long id;
    private Long lessonId;
    private String studentName;
    private String content;
    private LocalDateTime createdAt;
    private int answerCount;
    private String courseName;
    private List<AnswerDTO> answers;

    @Data
    @Builder
    public static class AnswerDTO {
        private Long id;
        private String responderName; // Đổi từ instructorName thành responderName (người trả lời chung)
        private boolean isInstructor; // 🔥 Thêm cờ này để Frontend trang trí
        private String content;
        private LocalDateTime createdAt;
    }
}