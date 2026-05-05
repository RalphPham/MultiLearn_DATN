package org.example.multileanproject.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.multileanproject.dto.BlogGenerateRequest;
import org.example.multileanproject.dto.BlogPostDTO;
import org.example.multileanproject.entity.BlogPost;
import org.example.multileanproject.entity.BlogStatus;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.repository.BlogPostRepository;
import org.example.multileanproject.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BlogPostServiceImpl implements BlogPostService {

    private final BlogPostRepository blogPostRepository;
    private final CourseRepository courseRepository;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=";

    // ─────────────────────────────────────────────────────────────
    // PUBLIC READ
    // ─────────────────────────────────────────────────────────────

    @Override
    public Page<BlogPostDTO> getPublishedPosts(String category, Pageable pageable) {
        if (category != null && !category.isBlank()) {
            return blogPostRepository
                    .findByCategoryAndStatusOrderByPublishedAtDesc(category, BlogStatus.PUBLISHED, pageable)
                    .map(BlogPostDTO::fromEntity);
        }
        return blogPostRepository
                .findByStatusOrderByPublishedAtDesc(BlogStatus.PUBLISHED, pageable)
                .map(BlogPostDTO::fromEntity);
    }

    @Override
    public BlogPostDTO getPostById(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));
        post.setViews(post.getViews() + 1);
        return BlogPostDTO.fromEntity(blogPostRepository.save(post));
    }

    // ─────────────────────────────────────────────────────────────
    // ADMIN
    // ─────────────────────────────────────────────────────────────

    @Override
    public Page<BlogPostDTO> getAllPosts(Pageable pageable) {
        return blogPostRepository.findAll(pageable).map(BlogPostDTO::fromEntity);
    }

    @Override
    public BlogPostDTO generatePost(BlogGenerateRequest request) {
        String prompt = buildPrompt(request);
        String rawJson = callGemini(prompt);
        return BlogPostDTO.fromEntity(blogPostRepository.save(parseAndBuild(rawJson, request)));
    }

    @Override
    public BlogPostDTO publishPost(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));
        post.setStatus(BlogStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        return BlogPostDTO.fromEntity(blogPostRepository.save(post));
    }

    @Override
    public void deletePost(Long id) {
        blogPostRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────
    // GEMINI INTEGRATION
    // ─────────────────────────────────────────────────────────────

    private String buildPrompt(BlogGenerateRequest request) {
        String type = request.getType() != null ? request.getType() : "TIPS";

        return switch (type) {
            case "SPOTLIGHT" -> buildSpotlightPrompt(request.getCourseId());
            case "TRENDING"  -> buildTrendingPrompt();
            case "WEEKLY"    -> buildWeeklyPrompt();
            default          -> buildTipsPrompt(request.getTopic());
        };
    }

    private String buildSpotlightPrompt(Long courseId) {
        if (courseId == null) return buildTipsPrompt(null);

        Course course = courseRepository.findByIdWithJoins(courseId).orElse(null);
        if (course == null) return buildTipsPrompt(null);

        String instructorName = (course.getInstructor() != null && course.getInstructor().getUser() != null)
                ? course.getInstructor().getUser().getFullName() : "Chưa cập nhật";
        String categoryName = course.getCategory() != null ? course.getCategory().getName() : "Chưa cập nhật";

        return """
                Bạn là biên tập viên của nền tảng học trực tuyến MultiLearn.
                Hãy viết một bài báo chuyên nghiệp, hấp dẫn và tự nhiên bằng tiếng Việt để giới thiệu khóa học sau:

                Tên khóa học: %s
                Danh mục: %s
                Giảng viên: %s
                Mô tả ngắn: %s
                Số học viên đăng ký: %d
                Điểm đánh giá trung bình: %.1f/5
                Giá: %s

                Yêu cầu:
                - Viết như phóng viên thực sự, không sáo rỗng, không giống template máy tạo
                - Đặt tiêu đề thật thu hút, mang tính báo chí
                - Bài viết có ít nhất 3 phần nội dung sâu
                - Đề cập đến lý do tại sao khóa học này đáng học, cơ hội nghề nghiệp liên quan
                - Dùng HTML: <h2>, <p>, <ul>, <li>, <strong>, <em>
                - Bài dài khoảng 450-600 từ
                - KHÔNG bắt đầu bằng "Giới thiệu", hãy mở đầu thật cuốn hút

                Trả về theo đúng định dạng sau (không thêm bất kỳ text nào khác):
                ###TITLE###
                tiêu đề bài viết
                ###EXCERPT###
                tóm tắt 1-2 câu thu hút
                ###CONTENT###
                nội dung HTML đầy đủ
                ###CATEGORY###
                NỔI BẬT
                ###READTIME###
                X phút đọc
                """.formatted(
                course.getTitle(),
                categoryName,
                instructorName,
                course.getShortDescription() != null ? course.getShortDescription() : "Không có mô tả",
                course.getStudentCount(),
                course.getAverageRating(),
                course.getPrice() != null && course.getPrice().doubleValue() > 0
                        ? String.format("%,.0f VNĐ", course.getPrice().doubleValue()) : "Miễn phí"
        );
    }

    private String buildTrendingPrompt() {
        List<Course> top5 = courseRepository.findTopPublishedByStudentCount(PageRequest.of(0, 5));

        StringBuilder courseList = new StringBuilder();
        int rank = 1;
        for (Course c : top5) {
            String cat = c.getCategory() != null ? c.getCategory().getName() : "Không phân loại";
            courseList.append("%d. \"%s\" (%s) — %d học viên, %.1f⭐\n"
                    .formatted(rank++, c.getTitle(), cat, c.getStudentCount(), c.getAverageRating()));
        }

        return """
                Bạn là biên tập viên của nền tảng học trực tuyến MultiLearn.
                Hãy viết một bài phân tích xu hướng học tập dựa trên dữ liệu thực tế dưới đây:

                Top khóa học được đăng ký nhiều nhất hiện tại:
                %s

                Yêu cầu:
                - Phân tích xu hướng, không chỉ đơn thuần liệt kê
                - Đưa ra lý do tại sao những lĩnh vực này đang hot
                - Liên kết với thị trường việc làm và xu hướng công nghệ
                - Dùng HTML: <h2>, <p>, <ul>, <li>, <strong>
                - Bài dài khoảng 400-500 từ, giọng điệu phân tích chuyên sâu

                Trả về theo đúng định dạng sau (không thêm bất kỳ text nào khác):
                ###TITLE###
                tiêu đề bài viết
                ###EXCERPT###
                tóm tắt 1-2 câu
                ###CONTENT###
                nội dung HTML đầy đủ
                ###CATEGORY###
                XU HƯỚNG
                ###READTIME###
                X phút đọc
                """.formatted(courseList.toString());
    }

    private String buildWeeklyPrompt() {
        long totalPublished = courseRepository.count();
        List<Course> recentCourses = courseRepository.findTopPublishedByStudentCount(PageRequest.of(0, 3));
        String topCourseName = recentCourses.isEmpty() ? "Chưa có dữ liệu" : recentCourses.get(0).getTitle();
        String week = LocalDateTime.now().format(DateTimeFormatter.ofPattern("'tuần' W 'tháng' M/yyyy"));

        return """
                Bạn là biên tập viên của nền tảng học trực tuyến MultiLearn.
                Hãy viết bản tổng kết hoạt động %s dựa trên số liệu thực tế:

                - Tổng số khóa học trên nền tảng: %d khóa học
                - Khóa học mới đáng chú ý: %s
                - Nền tảng đang phát triển với nhiều nội dung chất lượng

                Yêu cầu:
                - Viết như bản tin tổng kết, vừa chuyên nghiệp vừa thú vị để đọc
                - Highlight những điểm nổi bật, không chỉ liệt kê số liệu
                - Thêm phần dự báo/gợi ý cho tuần tới
                - Dùng HTML: <h2>, <p>, <ul>, <li>, <strong>
                - Bài dài khoảng 350-450 từ

                Trả về theo đúng định dạng sau (không thêm bất kỳ text nào khác):
                ###TITLE###
                tiêu đề bài viết
                ###EXCERPT###
                tóm tắt 1-2 câu
                ###CONTENT###
                nội dung HTML đầy đủ
                ###CATEGORY###
                NỔI BẬT
                ###READTIME###
                X phút đọc
                """.formatted(week, totalPublished, topCourseName);
    }

    private String buildTipsPrompt(String topic) {
        String subject = (topic != null && !topic.isBlank()) ? topic : "phương pháp học online hiệu quả và phát triển kỹ năng số";

        return """
                Bạn là biên tập viên của nền tảng học trực tuyến MultiLearn.
                Hãy viết một bài báo về chủ đề: %s

                Yêu cầu:
                - Giọng văn tự nhiên, gần gũi, thực tế — không giống bài viết AI sáo rỗng
                - Có ít nhất 4-5 mẹo/bí quyết cụ thể, ai cũng áp dụng được ngay
                - Mỗi mẹo phải kèm giải thích tại sao nó hiệu quả
                - Tránh các câu chung chung như "đặt mục tiêu rõ ràng"
                - Dùng HTML: <h2>, <p>, <ul>, <li>, <strong>, <em>
                - Bài dài khoảng 400-500 từ
                - Kết bài có phần call-to-action nhẹ nhàng liên quan đến học tập

                Trả về theo đúng định dạng sau (không thêm bất kỳ text nào khác):
                ###TITLE###
                tiêu đề bài viết
                ###EXCERPT###
                tóm tắt 1-2 câu
                ###CONTENT###
                nội dung HTML đầy đủ
                ###CATEGORY###
                MẸO HỌC TẬP
                ###READTIME###
                X phút đọc
                """.formatted(subject);
    }

    private String callGemini(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.8,
                        "maxOutputTokens", 2048
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

    private BlogPost parseAndBuild(String rawText, BlogGenerateRequest request) {
        String title    = extractSection(rawText, "###TITLE###",    "###EXCERPT###");
        String excerpt  = extractSection(rawText, "###EXCERPT###",  "###CONTENT###");
        String content  = extractSection(rawText, "###CONTENT###",  "###CATEGORY###");
        String category = extractSection(rawText, "###CATEGORY###", "###READTIME###");
        String readTime = extractSection(rawText, "###READTIME###", null);

        if (title.isBlank())    title    = "Bài viết mới";
        if (category.isBlank()) category = "NỔI BẬT";
        if (readTime.isBlank()) readTime = "3 phút đọc";

        log.info("Parsed blog → title='{}' category='{}' contentLen={}", title, category, content.length());

        // SPOTLIGHT → dùng ảnh của khóa học được chọn
        String thumbnail = null;
        if ("SPOTLIGHT".equals(request.getType()) && request.getCourseId() != null) {
            thumbnail = courseRepository.findByIdWithJoins(request.getCourseId())
                    .map(Course::getThumbnail).orElse(null);
        }
        // Mọi loại khác → Picsum với seed từ title: mỗi bài ảnh khác nhau
        if (thumbnail == null || thumbnail.isBlank()) {
            int seed = Math.abs(title.hashCode() % 1000);
            thumbnail = "https://picsum.photos/seed/" + seed + "/800/400";
        }

        return BlogPost.builder()
                .title(title)
                .excerpt(excerpt)
                .content(content)
                .thumbnail(thumbnail)
                .category(category)
                .readTime(readTime)
                .status(BlogStatus.DRAFT)
                .build();
    }

    /** Trích đoạn văn bản giữa startMarker và endMarker (hoặc cuối chuỗi nếu endMarker null). */
    private String extractSection(String text, String startMarker, String endMarker) {
        int s = text.indexOf(startMarker);
        if (s < 0) return "";
        s += startMarker.length();
        int e = (endMarker != null) ? text.indexOf(endMarker, s) : text.length();
        if (e < 0) e = text.length();
        return text.substring(s, e).trim();
    }
}
