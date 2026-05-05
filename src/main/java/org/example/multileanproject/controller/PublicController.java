package org.example.multileanproject.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.CouponDTO;
import org.example.multileanproject.dto.CourseDetailDTO;
import org.example.multileanproject.dto.CourseListDTO;
import org.example.multileanproject.dto.RatingResponseDTO;
import org.example.multileanproject.dto.SystemStats;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.CourseStatus;
import org.example.multileanproject.entity.Instructor;
import org.example.multileanproject.entity.OrderStatus;
import org.example.multileanproject.entity.Rating;
import org.example.multileanproject.entity.SaleCampaign;
import org.example.multileanproject.entity.SaleCampaignItem;
import org.example.multileanproject.repository.CouponRepository;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.EnrollmentRepository;
import org.example.multileanproject.repository.InstructorRepository;
import org.example.multileanproject.repository.OrderRepository;
import org.example.multileanproject.repository.RatingRepository;
import org.example.multileanproject.repository.SaleCampaignItemRepository;
import org.example.multileanproject.repository.SaleCampaignRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.service.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final CourseService              courseService;
    private final StudentRepository          studentRepository;
    private final InstructorRepository       instructorRepository;
    private final CourseRepository           courseRepository;
    private final CouponRepository           couponRepository;
    private final EnrollmentRepository       enrollmentRepository;
    private final OrderRepository            orderRepository;
    private final RatingRepository           ratingRepository;
    private final SaleCampaignRepository     saleCampaignRepository;
    private final SaleCampaignItemRepository saleCampaignItemRepository;

    // ── DTOs ─────────────────────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TopInstructorDTO {
        private Long   id;
        private String fullName;
        private String avatar;
        private String bio;
        private int    totalCourses;
        private int    totalStudents;
        private double avgRating;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LiveStatsDTO {
        private long   newEnrollmentsToday;
        private long   completedOrdersToday;
        private long   totalReviews;
        private double avgRatingToday;
    }

    // Dùng bởi GET /api/public/campaigns/active
    // Frontend BlogView.vue: activeCampaign ref
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CampaignPublicDTO {
        private Long          id;
        private String        name;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String        status;  // "ACTIVE" | "UPCOMING" | "ENDED" | "DISABLED"
    }

    // Dùng bởi GET /api/public/campaigns/active/items
    // Frontend BlogView.vue: campaignItems slider
    // SaleCampaignItem fields + Course fields
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CampaignItemPublicDTO {
        // From SaleCampaignItem
        private Long       id;           // SaleCampaignItem.id
        private BigDecimal promotionalPrice;  // → salePrice trong slider card
        private int        totalSlots;
        private int        soldSlots;
        // From SaleCampaignItem.course
        private Long       courseId;     // Course.id
        private String     title;
        private String     slug;
        private String     thumbnail;
        private BigDecimal price;        // giá gốc → tính % giảm: (1 - promo/price) * 100
        private String     categoryName;
        private Double     averageRating;
        private Integer    studentCount;
    }

    // ── CŨ: Courses ──────────────────────────────────────────────

    @GetMapping("/courses")
    public ResponseEntity<Page<CourseListDTO>> getPublicCourses(
            @RequestParam Map<String, String> params,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(courseService.searchCoursesPublic(params, page, size));
    }

    @GetMapping("/courses/slug/{slug}")
    public ResponseEntity<CourseDetailDTO> getCourseDetailBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(courseService.getCourseDetailBySlug(slug, getCurrentEmailOrNull()));
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<CourseDetailDTO> getCourseDetail(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseDetail(id, getCurrentEmailOrNull()));
    }

    // ── CŨ: Stats ────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<SystemStats> getPublicStats() {
        long totalStudents    = studentRepository.count();
        long totalInstructors = instructorRepository.count();
        long totalCourses     = courseRepository.countByStatus(CourseStatus.PUBLISHED);

        List<Course> published = courseRepository.findByStatus(CourseStatus.PUBLISHED);
        double averageRating = published.stream()
                .map(Course::getAverageRating)
                .filter(v -> v != null && v > 0)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(4.8);

        return ResponseEntity.ok(new SystemStats(
                totalStudents, totalCourses, totalInstructors,
                Math.round(averageRating * 10.0) / 10.0
        ));
    }

    // ── CŨ: Coupons ──────────────────────────────────────────────

    @GetMapping("/coupons/active")
    public ResponseEntity<List<CouponDTO>> getActiveCoupons() {
        LocalDateTime now = LocalDateTime.now();

        List<CouponDTO> result = couponRepository.findAll().stream()
                .filter(c -> {
                    // Platform-first: chỉ hiển thị coupon do nền tảng phát hành
                    // (instructor_id = null).
                    if (c.getInstructorId() != null) return false;

                    Boolean activeStatus = null;
                    try {
                        activeStatus = (Boolean) c.getClass().getMethod("getIsActive").invoke(c);
                    } catch (Exception e) {
                        try {
                            activeStatus = (Boolean) c.getClass().getMethod("isActive").invoke(c);
                        } catch (Exception ex) {
                            activeStatus = false;
                        }
                    }
                    if (!Boolean.TRUE.equals(activeStatus)) return false;
                    if (c.getEndDate() != null && c.getEndDate().isBefore(now)) return false;
                    if (c.getUsageLimit() != null && c.getUsedCount() != null
                            && c.getUsedCount() >= c.getUsageLimit()) return false;
                    return true;
                })
                .map(c -> {
                    CouponDTO dto = new CouponDTO();
                    dto.setId(c.getId());
                    dto.setCode(c.getCode());
                    dto.setDiscountType(c.getDiscountType());
                    dto.setDiscountValue(c.getDiscountValue());
                    dto.setMaxDiscountAmount(c.getMaxDiscountAmount());
                    dto.setUsageLimit(c.getUsageLimit());
                    int used = c.getUsedCount() != null ? c.getUsedCount() : 0;
                    dto.setUsedCount(used);
                    if (c.getUsageLimit() != null)
                        dto.setRemainingCount(c.getUsageLimit() - used);
                    dto.setStartDate(c.getStartDate());
                    dto.setEndDate(c.getEndDate());
                    dto.setMinOrderValue(c.getMinOrderValue());
                    Boolean activeStatus = null;
                    try {
                        activeStatus = (Boolean) c.getClass().getMethod("getIsActive").invoke(c);
                    } catch (Exception e) {
                        try {
                            activeStatus = (Boolean) c.getClass().getMethod("isActive").invoke(c);
                        } catch (Exception ex) {
                            activeStatus = false;
                        }
                    }
                    dto.setActive(Boolean.TRUE.equals(activeStatus));
                    return dto;
                })
                .sorted((a, b) -> {
                    if (a.getRemainingCount() == null && b.getRemainingCount() == null) return 0;
                    if (a.getRemainingCount() == null) return 1;
                    if (b.getRemainingCount() == null) return -1;
                    return Integer.compare(a.getRemainingCount(), b.getRemainingCount());
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── CŨ: Top Instructors ──────────────────────────────────────

    @GetMapping("/instructors/top")
    public ResponseEntity<List<TopInstructorDTO>> getTopInstructors(
            @RequestParam(defaultValue = "6") int size
    ) {
        List<TopInstructorDTO> result = instructorRepository.findAll().stream()
                .filter(i -> {
                    Boolean activeStatus = null;
                    try {
                        activeStatus = (Boolean) i.getClass().getMethod("getIsActive").invoke(i);
                    } catch (Exception e) {
                        try {
                            activeStatus = (Boolean) i.getClass().getMethod("isActive").invoke(i);
                        } catch (Exception ex) {
                            activeStatus = false;
                        }
                    }
                    return Boolean.TRUE.equals(activeStatus);
                })
                .map(this::buildTopInstructorDTO)
                .filter(dto -> dto != null && dto.getTotalCourses() > 0)
                .sorted((a, b) -> Integer.compare(b.getTotalStudents(), a.getTotalStudents()))
                .limit(size)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Public profile giảng viên (ai cũng xem được).
     * Hỗ trợ cả singular lẫn plural để tương thích frontend cũ/mới.
     */
    @GetMapping({"/instructor/{id}", "/instructors/{id}"})
    public ResponseEntity<?> getInstructorPublicProfile(@PathVariable Long id) {
        Instructor instructor = instructorRepository.findByUser_Id(id)
                .orElseGet(() -> instructorRepository.findById(id).orElse(null));

        if (instructor == null) {
            return ResponseEntity.notFound().build();
        }

        Long instructorUserId = instructor.getUser() != null ? instructor.getUser().getId() : null;
        if (instructorUserId == null) {
            return ResponseEntity.notFound().build();
        }

        List<Course> publishedCourses = courseRepository.findByInstructorUserId(instructorUserId).stream()
                .filter(c -> c.getStatus() == CourseStatus.PUBLISHED)
                .collect(Collectors.toList());

        List<Map<String, Object>> courses = publishedCourses.stream()
                .map(this::toPublicInstructorCourse)
                .collect(Collectors.toList());

        int totalStudents = publishedCourses.stream()
                .mapToInt(c -> c.getStudentCount() != null ? c.getStudentCount() : 0)
                .sum();

        double avgRating = publishedCourses.stream()
                .map(Course::getAverageRating)
                .filter(v -> v != null && v > 0)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        int ratingCount = ratingRepository.findByCourse_Instructor_IdOrderByCreatedAtDesc(instructor.getId()).size();

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", instructorUserId); // route id frontend dùng userId
        profile.put("instructorId", instructor.getId());
        profile.put("fullName", firstNonBlank(
                instructor.getFullName(),
                instructor.getUser() != null ? instructor.getUser().getFullName() : null,
                "Giảng viên"
        ));
        profile.put("avatar", firstNonBlank(
                instructor.getAvatarUrl(),
                instructor.getUser() != null ? instructor.getUser().getAvatar() : null,
                null
        ));
        profile.put("bio", instructor.getBio());
        profile.put("totalCourses", courses.size());
        profile.put("totalStudents", totalStudents);
        profile.put("avgRating", Math.round(avgRating * 10.0) / 10.0);
        profile.put("ratingCount", ratingCount);
        profile.put("hashtags", buildInstructorHashtags(instructor, publishedCourses));
        profile.put("courses", courses);

        return ResponseEntity.ok(profile);
    }

    // ── CŨ: Live Stats ───────────────────────────────────────────

    @GetMapping("/live-stats")
    public ResponseEntity<LiveStatsDTO> getLiveStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = LocalDate.now().atTime(LocalTime.MAX);

        long completedOrdersToday = 0L;
        try {
            completedOrdersToday = orderRepository
                    .findByStatusAndCreatedAtBetween(OrderStatus.COMPLETED, startOfDay, endOfDay)
                    .size();
        } catch (Exception ignored) {}

        long totalReviews = 0L;
        try { totalReviews = ratingRepository.count(); } catch (Exception ignored) {}

        double avgRatingToday = 0.0;
        try {
            List<Rating> allRatings = ratingRepository.findAll();
            double avg = allRatings.stream()
                    .filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().isBefore(startOfDay))
                    .mapToInt(Rating::getStars)
                    .average()
                    .orElse(0.0);
            avgRatingToday = Math.round(avg * 10.0) / 10.0;
        } catch (Exception ignored) {}

        return ResponseEntity.ok(LiveStatsDTO.builder()
                .newEnrollmentsToday(0L)   // cần Enrollment.createdAt — thêm sau nếu có
                .completedOrdersToday(completedOrdersToday)
                .totalReviews(totalReviews)
                .avgRatingToday(avgRatingToday)
                .build());
    }

    // ── CŨ: Recent Reviews ───────────────────────────────────────

    @GetMapping("/reviews/recent")
    public ResponseEntity<List<RatingResponseDTO>> getRecentReviews(
            @RequestParam(defaultValue = "6") int size
    ) {
        List<RatingResponseDTO> result;
        try {
            List<Rating> allRatings = ratingRepository.findAll();
            result = allRatings.stream()
                    .filter(r -> r.getComment() != null && !r.getComment().isBlank() && r.getStars() >= 4)
                    .filter(r -> r.getCourse() != null && r.getCourse().getStatus() == CourseStatus.PUBLISHED)
                    .sorted((r1, r2) -> {
                        if (r1.getCreatedAt() == null) return 1;
                        if (r2.getCreatedAt() == null) return -1;
                        return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                    })
                    .map(r -> RatingResponseDTO.builder()
                            .id(r.getId())
                            .stars(r.getStars())
                            .comment(r.getComment())
                            .studentId(r.getStudent()   != null ? r.getStudent().getId()        : null)
                            .studentName(r.getStudent() != null ? r.getStudent().getFullName()   : null)
                            .studentAvatar(r.getStudent() != null ? r.getStudent().getAvatar()   : null)
                            .courseId(r.getCourse()     != null ? r.getCourse().getId()          : null)
                            .courseTitle(r.getCourse()  != null ? r.getCourse().getTitle()       : null)
                            .courseSlug(r.getCourse()   != null ? r.getCourse().getSlug()        : null)
                            .courseThumbnail(r.getCourse() != null ? r.getCourse().getThumbnail(): null)
                            .createdAt(r.getCreatedAt())
                            .build())
                    .limit(size)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            result = List.of();
        }
        return ResponseEntity.ok(result);
    }

    // ════════════════════════════════════════════════════════════
    // MỚI: GET /api/public/campaigns/active
    //
    // Dùng SaleCampaignRepository.findCurrentlyActiveCampaigns()
    // → trả về List<SaleCampaign>, lấy phần tử đầu tiên
    //
    // Response: CampaignPublicDTO (200)
    //           hoặc 204 No Content nếu không có campaign nào
    //
    // Frontend: BlogView.vue → fetchActiveCampaignAndItems()
    //   axiosClient.get('/public/campaigns/active')
    // ════════════════════════════════════════════════════════════
    @GetMapping("/campaigns/active")
    public ResponseEntity<?> getActiveCampaign() {
        try {
            // findCurrentlyActiveCampaigns() — method có sẵn trong SaleCampaignRepository
            List<SaleCampaign> activeCampaigns = saleCampaignRepository.findCurrentlyActiveCampaigns();

            if (activeCampaigns == null || activeCampaigns.isEmpty()) {
                return ResponseEntity.noContent().build(); // 204 → frontend xử lý null
            }

            // Lấy campaign đầu tiên (sắp xếp endDate ASC → sắp hết hạn nhất lên đầu)
            SaleCampaign campaign = activeCampaigns.get(0);

            return ResponseEntity.ok(CampaignPublicDTO.builder()
                    .id(campaign.getId())
                    .name(campaign.getName())
                    .startDate(campaign.getStartDate())
                    .endDate(campaign.getEndDate())
                    .status(campaign.getStatus())  // @Transient getStatus() trong SaleCampaign entity
                    .build());

        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }

    // ════════════════════════════════════════════════════════════
    // MỚI: GET /api/public/campaigns/active/items
    //
    // Dùng SaleCampaignItemRepository.findByCampaignId(Long)
    // → có sẵn trong SaleCampaignItemRepository
    //
    // Filter thêm client-side: chỉ lấy course PUBLISHED
    // Sắp xếp: slot còn ít nhất lên đầu (hiệu ứng khan hiếm / FOMO)
    //
    // Response: List<CampaignItemPublicDTO>
    // Frontend: BlogView.vue → campaignItems slider
    //   item.promotionalPrice → salePrice trong card
    //   item.price (giá gốc) → tính % giảm
    //   item.totalSlots, soldSlots → progress bar
    // ════════════════════════════════════════════════════════════
    @GetMapping("/campaigns/active/items")
    public ResponseEntity<List<CampaignItemPublicDTO>> getActiveCampaignItems() {
        try {
            List<SaleCampaign> activeCampaigns = saleCampaignRepository.findCurrentlyActiveCampaigns();

            if (activeCampaigns == null || activeCampaigns.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            Long campaignId = activeCampaigns.get(0).getId();

            // findByCampaignId() — method có sẵn trong SaleCampaignItemRepository
            List<CampaignItemPublicDTO> items = saleCampaignItemRepository
                    .findByCampaignId(campaignId)
                    .stream()
                    .filter(item -> item.getCourse() != null
                            && item.getCourse().getStatus() == CourseStatus.PUBLISHED)
                    // Loại bỏ item đã hết slot
                    .filter(item -> item.getSoldSlots() < item.getTotalSlots())
                    .map(item -> {
                        Course c = item.getCourse();

                        // categoryName — lấy từ course.category nếu entity có quan hệ
                        String catName = null;
                        try {
                            catName = c.getCategory() != null ? c.getCategory().getName() : null;
                        } catch (Exception ignored) {}

                        return CampaignItemPublicDTO.builder()
                                .id(item.getId())
                                .promotionalPrice(item.getPromotionalPrice())
                                .totalSlots(item.getTotalSlots())
                                .soldSlots(item.getSoldSlots())
                                .courseId(c.getId())
                                .title(c.getTitle())
                                .slug(c.getSlug())
                                .thumbnail(c.getThumbnail())
                                .price(c.getPrice())
                                .categoryName(catName)
                                .averageRating(c.getAverageRating())
                                .studentCount(c.getStudentCount())
                                .build();
                    })
                    // FOMO: slot còn ít nhất lên đầu slider
                    .sorted((a, b) -> Integer.compare(
                            a.getTotalSlots() - a.getSoldSlots(),
                            b.getTotalSlots() - b.getSoldSlots()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(items);

        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────

    private TopInstructorDTO buildTopInstructorDTO(Instructor instructor) {
        try {
            List<Course> allCourses = courseRepository.findAll();
            List<Course> published = allCourses.stream()
                    .filter(c -> c.getInstructor() != null
                            && Objects.equals(c.getInstructor().getId(), instructor.getId())
                            && c.getStatus() == CourseStatus.PUBLISHED)
                    .collect(Collectors.toList());

            int totalCourses  = published.size();
            int totalStudents = published.stream()
                    .mapToInt(c -> c.getStudentCount() != null ? c.getStudentCount() : 0).sum();
            double avgRating  = published.stream()
                    .map(Course::getAverageRating).filter(r -> r != null && r > 0)
                    .mapToDouble(Double::doubleValue).average().orElse(0.0);

            String fullName = instructor.getFullName();
            if ((fullName == null || fullName.isBlank()) && instructor.getUser() != null)
                fullName = instructor.getUser().getFullName();

            String avatar = instructor.getAvatarUrl();
            if ((avatar == null || avatar.isBlank()) && instructor.getUser() != null)
                avatar = instructor.getUser().getAvatar();

            return TopInstructorDTO.builder()
                    .id(instructor.getId())
                    .fullName(fullName)
                    .avatar(avatar)
                    .bio(instructor.getBio())
                    .totalCourses(totalCourses)
                    .totalStudents(totalStudents)
                    .avgRating(Math.round(avgRating * 10.0) / 10.0)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> toPublicInstructorCourse(Course course) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", course.getId());
        data.put("title", course.getTitle());
        data.put("slug", course.getSlug());
        data.put("thumbnail", course.getThumbnail());
        data.put("shortDescription", course.getShortDescription());
        data.put("description", course.getDescription());
        data.put("price", course.getPrice());
        data.put("salePrice", course.getSalePrice());
        data.put("level", course.getLevel());
        data.put("averageRating", course.getAverageRating());
        data.put("studentCount", course.getStudentCount());
        data.put("categoryName", course.getCategory() != null ? course.getCategory().getName() : null);
        return data;
    }

    private List<String> buildInstructorHashtags(Instructor instructor, List<Course> courses) {
        List<String> tags = new ArrayList<>();
        if (instructor != null && instructor.getBio() != null) {
            Arrays.stream(instructor.getBio().split("[,;|]"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .forEach(tags::add);
        }
        courses.stream()
                .map(c -> c.getCategory() != null ? c.getCategory().getName() : null)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .forEach(tags::add);

        return tags.stream()
                .map(s -> s.startsWith("#") ? s.substring(1) : s)
                .filter(s -> !s.isBlank())
                .map(String::trim)
                .distinct()
                .limit(12)
                .collect(Collectors.toList());
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String getCurrentEmailOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        if (auth instanceof AnonymousAuthenticationToken) return null;
        String name = auth.getName();
        return (name == null || "anonymousUser".equalsIgnoreCase(name)) ? null : name;
    }
}
