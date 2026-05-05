package org.example.multileanproject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.multileanproject.dto.MarketInsightRequest;
import org.example.multileanproject.dto.MarketInsightResponse;
import org.example.multileanproject.entity.Category;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.repository.CategoryRepository;
import org.example.multileanproject.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketInsightServiceImpl implements MarketInsightService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=";

    @Override
    public MarketInsightResponse analyze(MarketInsightRequest request) {
        // 1. Lấy top 15 khóa học hot nhất từ DB
        List<Course> topCourses = courseRepository.findTopPublishedByStudentCount(PageRequest.of(0, 15));

        // 2. Lấy danh mục có khóa học published
        List<Category> categories = categoryRepository.findCategoriesWithPublishedCourses();

        // 3. Build context string từ dữ liệu thực tế
        String courseContext = buildCourseContext(topCourses);
        String categoryContext = buildCategoryContext(categories);

        // 4. Gọi Gemini
        String prompt = buildPrompt(request.getTopic(), courseContext, categoryContext);
        String raw = callGemini(prompt);

        // 5. Parse kết quả
        String trending     = extractSection(raw, "###TRENDING###",        "###DEMAND###");
        String demand       = extractSection(raw, "###DEMAND###",           "###OPPORTUNITIES###");
        String opportunities = extractSection(raw, "###OPPORTUNITIES###",  "###RECOMMENDATIONS###");
        String recommendations = extractSection(raw, "###RECOMMENDATIONS###", "###END###");

        // 6. Build danh sách hot courses cho frontend
        List<MarketInsightResponse.HotCourse> hotCourses = topCourses.stream()
                .limit(8)
                .map(c -> MarketInsightResponse.HotCourse.builder()
                        .title(c.getTitle())
                        .category(c.getCategory() != null ? c.getCategory().getName() : "Khác")
                        .students(c.getStudentCount())
                        .rating(c.getAverageRating())
                        .build())
                .collect(Collectors.toList());

        return MarketInsightResponse.builder()
                .trendingTopics(trending.isBlank() ? "Không có dữ liệu" : trending)
                .demandAnalysis(demand.isBlank() ? "Không có dữ liệu" : demand)
                .opportunities(opportunities.isBlank() ? "Không có dữ liệu" : opportunities)
                .recommendations(recommendations.isBlank() ? "Không có dữ liệu" : recommendations)
                .hotCourses(hotCourses)
                .build();
    }

    private String buildCourseContext(List<Course> courses) {
        if (courses.isEmpty()) return "Chưa có khóa học nào.";
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (Course c : courses) {
            String cat = c.getCategory() != null ? c.getCategory().getName() : "Không rõ";
            sb.append(i++).append(". \"").append(c.getTitle()).append("\"")
              .append(" | Danh mục: ").append(cat)
              .append(" | Học viên: ").append(c.getStudentCount())
              .append(" | Đánh giá: ").append(String.format("%.1f", c.getAverageRating())).append("/5")
              .append("\n");
        }
        return sb.toString();
    }

    private String buildCategoryContext(List<Category> categories) {
        if (categories.isEmpty()) return "Chưa có danh mục nào.";
        return categories.stream()
                .map(cat -> "- " + cat.getName())
                .collect(Collectors.joining("\n"));
    }

    private String buildPrompt(String topic, String courseContext, String categoryContext) {
        String topicSection = (topic != null && !topic.isBlank())
                ? "\nCHỦ ĐỀ GIẢNG VIÊN QUAN TÂM: " + topic + "\n(Hãy tập trung phân tích liên quan đến chủ đề này nếu phù hợp)\n"
                : "";

        return """
                Bạn là chuyên gia phân tích thị trường e-learning tại Việt Nam, am hiểu nhu cầu học trực tuyến.

                DỮ LIỆU THỰC TẾ - TOP 15 KHÓA HỌC NHIỀU HỌC VIÊN NHẤT TRÊN NỀN TẢNG:
                %s

                CÁC DANH MỤC CÓ TRÊN NỀN TẢNG:
                %s
                %s
                Hãy phân tích thị trường e-learning Việt Nam dựa trên dữ liệu trên. Trả lời ngắn gọn, thực tế, bằng tiếng Việt, theo đúng format dưới đây (giữ nguyên các marker ###):

                ###TRENDING###
                Liệt kê 4-5 chủ đề/lĩnh vực đang được quan tâm nhiều nhất. Mỗi mục một dòng, bắt đầu bằng dấu •.
                ###DEMAND###
                Phân tích ngắn (3-4 câu) về nhu cầu học viên hiện tại: họ đang tìm kiếm gì, lĩnh vực nào thiếu khóa học chất lượng.
                ###OPPORTUNITIES###
                Liệt kê 3 cơ hội cụ thể cho giảng viên muốn tạo khóa học mới. Mỗi mục một dòng, bắt đầu bằng dấu •.
                ###RECOMMENDATIONS###
                Đưa ra 3-4 lời khuyên cụ thể, thực tế để giảng viên thành công trên nền tảng. Mỗi mục một dòng, bắt đầu bằng dấu •.
                ###END###
                """.formatted(courseContext, categoryContext, topicSection);
    }

    private String callGemini(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 1024
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    GEMINI_URL + geminiApiKey,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            JsonNode root = new ObjectMapper().readTree(response.getBody());
            return root.path("candidates").get(0)
                       .path("content").path("parts").get(0)
                       .path("text").asText();
        } catch (Exception e) {
            log.error("Lỗi gọi Gemini API: {}", e.getMessage());
            throw new RuntimeException("Không thể kết nối Gemini API: " + e.getMessage());
        }
    }

    private String extractSection(String text, String startMarker, String endMarker) {
        int s = text.indexOf(startMarker);
        if (s < 0) return "";
        s += startMarker.length();
        int e = (endMarker != null) ? text.indexOf(endMarker, s) : text.length();
        if (e < 0) e = text.length();
        return text.substring(s, e).trim();
    }
}
