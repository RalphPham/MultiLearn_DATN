package org.example.multileanproject.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.multileanproject.dto.CourseListDTO;
import org.example.multileanproject.entity.Enrollment;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.EnrollmentRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class RecommendationController {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    /**
     * GET /api/student/recommendations
     * Dùng Gemini AI để gợi ý khóa học phù hợp dựa trên lịch sử học của student.
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<CourseListDTO>> getRecommendations(Authentication authentication) {
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();

        var studentOpt = studentRepository.findByEmail(email);
        if (studentOpt.isEmpty()) return ResponseEntity.ok(List.of());

        Long studentId = studentOpt.get().getId();

        // 1. Lịch sử học: các khóa đang enrolled
        List<Enrollment> enrolled = enrollmentRepository.findByStudent_IdAndStatus(studentId, "ACTIVE");
        if (enrolled.isEmpty()) {
            // Chưa có khóa nào → trả về top 8 phổ biến
            return ResponseEntity.ok(mapRows(courseRepository.findPopularExcludingEnrolled(email)));
        }

        // 2. Candidate courses: top 20 phổ biến chưa enrolled
        List<Object[]> candidates = courseRepository.findPopularExcludingEnrolled(email);
        // Override TOP limit — query trả tối đa 8, cần nhiều hơn để AI chọn
        List<Object[]> candidatesExtended = courseRepository.findTop20PopularExcludingEnrolled(email);
        if (candidatesExtended.isEmpty()) return ResponseEntity.ok(List.of());

        // 3. Build prompt cho Gemini
        String prompt = buildPrompt(enrolled, candidatesExtended);

        // 4. Gọi Gemini lấy danh sách ID
        List<Long> recommendedIds = callGeminiForIds(prompt);

        // 5. Nếu Gemini thất bại → fallback category-based
        if (recommendedIds.isEmpty()) {
            List<Object[]> fallback = courseRepository.findRecommendedForStudent(email);
            if (fallback.isEmpty()) fallback = candidatesExtended;
            return ResponseEntity.ok(mapRows(fallback.subList(0, Math.min(8, fallback.size()))));
        }

        // 6. Map candidates theo thứ tự Gemini trả về
        Map<Long, Object[]> candidateMap = new LinkedHashMap<>();
        for (Object[] row : candidatesExtended) {
            Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
            if (id != null) candidateMap.put(id, row);
        }

        List<Object[]> ordered = recommendedIds.stream()
                .filter(candidateMap::containsKey)
                .map(candidateMap::get)
                .limit(8)
                .collect(Collectors.toList());

        // Nếu AI trả về ít hơn 8, bổ sung từ candidates
        if (ordered.size() < 8) {
            Set<Long> usedIds = new HashSet<>(recommendedIds);
            for (Object[] row : candidatesExtended) {
                if (ordered.size() >= 8) break;
                Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
                if (id != null && !usedIds.contains(id)) {
                    ordered.add(row);
                    usedIds.add(id);
                }
            }
        }

        return ResponseEntity.ok(mapRows(ordered));
    }

    // ─────────────────────────────────────────────────────────────────────────

    private String buildPrompt(List<Enrollment> enrolled, List<Object[]> candidates) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là hệ thống gợi ý khóa học thông minh.\n\n");

        sb.append("LỊCH SỬ HỌC CỦA HỌC VIÊN:\n");
        for (Enrollment e : enrolled) {
            var course = e.getCourse();
            String cat = course.getCategory() != null ? course.getCategory().getName() : "Chưa phân loại";
            sb.append(String.format("- [%s] %s (hoàn thành: %s)\n",
                    cat, course.getTitle(),
                    Boolean.TRUE.equals(e.getIsCourseCompleted()) ? "Có" : "Chưa"));
        }

        sb.append("\nCÁC KHÓA HỌC ĐỀ XUẤT (ứng viên):\n");
        for (Object[] row : candidates) {
            Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
            String title = row[1] != null ? row[1].toString() : "";
            String cat = row[11] != null ? row[11].toString() : "";
            double rating = row[7] != null ? ((Number) row[7]).doubleValue() : 0;
            sb.append(String.format("- ID:%d | [%s] %s | ★%.1f\n", id, cat, title, rating));
        }

        sb.append("\nDựa trên lịch sử học, hãy chọn 8 khóa học PHÙ HỢP NHẤT cho học viên này.\n");
        sb.append("Ưu tiên: cùng lĩnh vực, nâng cao kỹ năng đang học, hoặc bổ sung kiến thức liên quan.\n");
        sb.append("Trả lời CHỈ gồm danh sách ID theo thứ tự ưu tiên, cách nhau bởi dấu phẩy.\n");
        sb.append("Ví dụ: 12,5,8,3,15,7,2,9\n");
        sb.append("Không giải thích, không thêm text khác.");

        return sb.toString();
    }

    private List<Long> callGeminiForIds(String prompt) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", prompt))
                    ))
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    GEMINI_URL + geminiApiKey,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            JsonNode root = new ObjectMapper().readTree(response.getBody());
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText().trim();

            // Parse "12,5,8,3,15,7,2,9"
            List<Long> ids = new ArrayList<>();
            for (String part : text.split("[,\\s]+")) {
                try {
                    ids.add(Long.parseLong(part.trim()));
                } catch (NumberFormatException ignored) {}
            }
            return ids;

        } catch (Exception e) {
            log.warn("Gemini recommendation failed, using fallback: {}", e.getMessage());
            return List.of();
        }
    }

    private List<CourseListDTO> mapRows(List<Object[]> rows) {
        List<CourseListDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(CourseListDTO.builder()
                    .id(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .title(row[1] != null ? row[1].toString() : "")
                    .slug(row[2] != null ? row[2].toString() : "")
                    .thumbnail(row[3] != null ? row[3].toString() : null)
                    .price(row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO)
                    .salePrice(row[5] != null ? new BigDecimal(row[5].toString()) : null)
                    .level(row[6] != null ? row[6].toString() : null)
                    .averageRating(row[7] != null ? ((Number) row[7]).doubleValue() : 0.0)
                    .studentCount(row[8] != null ? ((Number) row[8]).intValue() : 0)
                    .totalLessons(row[9] != null ? ((Number) row[9]).intValue() : 0)
                    .totalDuration(row[10] != null ? ((Number) row[10]).intValue() : 0)
                    .categoryName(row[11] != null ? row[11].toString() : "")
                    .instructorName(row[12] != null ? row[12].toString() : "")
                    .instructorAvatar(row[13] != null ? row[13].toString() : null)
                    .isFlashSale(false)
                    .build());
        }
        return result;
    }
}
