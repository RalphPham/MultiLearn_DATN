package org.example.multileanproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.InstructorCourseEnrollmentDTO;
import org.example.multileanproject.dto.InstructorRevenueSummaryDTO;
import org.example.multileanproject.dto.InstructorStudentItemDTO;
import org.example.multileanproject.dto.InstructorStudentOverviewDTO;
import org.example.multileanproject.dto.LessonDropoffDTO;
import org.example.multileanproject.dto.RatingTrendDTO;
import org.example.multileanproject.dto.RecentTransactionDTO;
import org.example.multileanproject.dto.RevenueChartPointDTO;
import org.example.multileanproject.dto.RevenueMetricsDTO;
import org.example.multileanproject.dto.TopCourseRevenueDTO;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.EnrollmentRepository;
import org.example.multileanproject.repository.LearningProgressRepository;
import org.example.multileanproject.repository.OrderRepository;
import org.example.multileanproject.repository.RatingRepository;
import org.example.multileanproject.repository.projection.InstructorCourseEnrollmentView;
import org.example.multileanproject.service.InstructorPerformanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorPerformanceServiceImpl implements InstructorPerformanceService {

    private final EnrollmentRepository enrollmentRepository;
    private final OrderRepository orderRepository;
    private final CourseRepository courseRepository;
    private final LearningProgressRepository progressRepository;
    private final RatingRepository ratingRepository;

    @Override
    public InstructorStudentOverviewDTO getStudentOverview(String instructorEmail) {
        List<InstructorCourseEnrollmentView> rows =
                enrollmentRepository.findCourseEnrollmentStatsByInstructorEmail(instructorEmail);

        List<InstructorCourseEnrollmentDTO> courses = rows.stream()
                .map(row -> new InstructorCourseEnrollmentDTO(
                        row.getCourseId(),
                        row.getCourseTitle(),
                        row.getCourseSlug(),
                        row.getActiveEnrollments() == null ? 0L : row.getActiveEnrollments(),
                        row.getUniqueStudents() == null ? 0L : row.getUniqueStudents()
                ))
                .toList();

        Long totalUniqueStudents = enrollmentRepository.countDistinctStudentsByInstructorEmail(instructorEmail);
        Long totalActiveEnrollments = enrollmentRepository.countActiveEnrollmentsByInstructorEmail(instructorEmail);

        if (totalUniqueStudents == null) totalUniqueStudents = 0L;
        if (totalActiveEnrollments == null) totalActiveEnrollments = 0L;

        return InstructorStudentOverviewDTO.builder()
                .instructorEmail(instructorEmail)
                .totalCourses((long) courses.size())
                .totalUniqueStudents(totalUniqueStudents)
                .totalActiveEnrollments(totalActiveEnrollments)
                .courses(courses)
                .build();
    }

    @Override
    public List<InstructorStudentItemDTO> getStudentItems(String instructorEmail) {
        return mapStudentItemRows(enrollmentRepository.findStudentItemsByInstructorEmail(instructorEmail));
    }

    @Override
    public List<InstructorStudentItemDTO> getAtRiskStudents(String instructorEmail) {
        return mapStudentItemRows(enrollmentRepository.findAtRiskStudentsByInstructorEmail(instructorEmail));
    }

    private List<InstructorStudentItemDTO> mapStudentItemRows(
            List<org.example.multileanproject.repository.projection.InstructorStudentItemView> rows) {
        return rows.stream()
                .map(row -> new InstructorStudentItemDTO(
                        row.getEnrollmentId(),
                        row.getStudentId(),
                        row.getStudentName(),
                        row.getStudentEmail(),
                        row.getStudentAvatar(),
                        row.getCourseId(),
                        row.getCourseTitle(),
                        row.getCourseSlug(),
                        row.getCourseThumbnail(),
                        row.getEnrollmentStatus(),
                        row.getProgress() != null ? row.getProgress() : 0.0,
                        row.getEnrolledAt()
                ))
                .toList();
    }

    @Override
    public InstructorRevenueSummaryDTO getRevenueSummary(String instructorEmail, Integer year,
                                                         LocalDate fromDate, LocalDate toDate) {
        // Chuyển year / dateRange thành LocalDateTime để query
        LocalDateTime from;
        LocalDateTime to;
        if (fromDate != null || toDate != null) {
            from = (fromDate != null) ? fromDate.atStartOfDay() : null;
            to   = (toDate   != null) ? toDate.plusDays(1).atStartOfDay() : null;
        } else if (year != null) {
            from = LocalDateTime.of(year, 1, 1, 0, 0);
            to   = LocalDateTime.of(year + 1, 1, 1, 0, 0);
        } else {
            from = null;
            to   = null;
        }

        // Metrics (aggregate toàn bộ khoảng thời gian)
        List<Object[]> metricRows = orderRepository.calculateRevenueMetrics(instructorEmail, from, to);
        Object[] row = (metricRows != null && !metricRows.isEmpty()) ? metricRows.get(0) : null;
        RevenueMetricsDTO metrics;
        if (row == null || row.length < 5) {
            metrics = new RevenueMetricsDTO(0.0, 0.0, 0.0, 0L, 0L);
        } else {
            metrics = new RevenueMetricsDTO(
                    toDouble(row[0]),
                    toDouble(row[1]),
                    toDouble(row[2]),
                    toLong(row[3]),
                    toLong(row[4])
            );
        }

        // Chart — trả về (year, month, gross, refund, net)
        List<RevenueChartPointDTO> chart = orderRepository.getRevenueChart(instructorEmail, from, to)
                .stream()
                .map(r -> new RevenueChartPointDTO(
                        toInt(r[0]),        // year
                        toInt(r[1]),        // month
                        toBigDecimal(r[2]), // gross
                        toBigDecimal(r[3]), // refund
                        toBigDecimal(r[4])  // net
                ))
                .toList();

        List<TopCourseRevenueDTO> topCourses = courseRepository.getTopRevenueCourses(instructorEmail, from, to);
        List<RecentTransactionDTO> transactions = orderRepository.getRecentTransactions(instructorEmail, from, to);

        return new InstructorRevenueSummaryDTO(
                metrics,
                chart,
                topCourses == null ? List.of() : topCourses,
                transactions == null ? List.of() : transactions
        );
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        return 0.0;
    }

    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        return 0L;
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        return 0;
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal bd) return bd;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        return BigDecimal.ZERO;
    }

    @Override
    public List<LessonDropoffDTO> getLessonDropoff(String instructorEmail, Long courseId) {
        // Verify course belongs to this instructor
        boolean owned = courseRepository.findByInstructorEmail(instructorEmail)
                .stream().anyMatch(c -> c.getId().equals(courseId));
        if (!owned) {
            throw new RuntimeException("Bạn không có quyền xem phân tích khóa học này.");
        }

        long enrolled = enrollmentRepository.countByCourse_IdAndStatusNot(courseId, "REFUNDED");

        List<Object[]> rows = progressRepository.getLessonCompletionsByCourse(courseId);
        List<LessonDropoffDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long lessonId     = ((Number) row[0]).longValue();
            String lessonTitle = (String) row[1];
            String sectionTitle = (String) row[2];
            long completed    = ((Number) row[5]).longValue();
            result.add(new LessonDropoffDTO(lessonId, lessonTitle, sectionTitle, completed, enrolled));
        }
        return result;
    }

    @Override
    public RatingTrendDTO getRatingTrend(String instructorEmail) {
        // 1. Monthly trend (tất cả thời gian, lấy 6 tháng gần nhất)
        List<Object[]> trendRows = ratingRepository.findMonthlyTrendByInstructorEmail(instructorEmail);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");

        // Build frame 6 tháng gần nhất
        Map<String, RatingTrendDTO.MonthlyPoint> frame = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            String key = d.format(fmt);
            frame.put(key, RatingTrendDTO.MonthlyPoint.builder()
                    .label(key).avgRating(null).count(0L).build());
        }

        for (Object[] row : trendRows) {
            int year  = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            double avg = ((Number) row[2]).doubleValue();
            long cnt   = ((Number) row[3]).longValue();
            String key = String.format("%02d/%04d", month, year);
            if (frame.containsKey(key)) {
                frame.put(key, RatingTrendDTO.MonthlyPoint.builder()
                        .label(key)
                        .avgRating(Math.round(avg * 10.0) / 10.0)
                        .count(cnt)
                        .build());
            }
        }

        List<RatingTrendDTO.MonthlyPoint> monthly = new ArrayList<>(frame.values());

        // 2. Trend direction: so sánh avg của 3 tháng gần vs 3 tháng trước
        List<Double> recentAvgs = monthly.subList(3, 6).stream()
                .filter(p -> p.getAvgRating() != null).map(RatingTrendDTO.MonthlyPoint::getAvgRating).toList();
        List<Double> prevAvgs = monthly.subList(0, 3).stream()
                .filter(p -> p.getAvgRating() != null).map(RatingTrendDTO.MonthlyPoint::getAvgRating).toList();

        String direction = "STABLE";
        if (!recentAvgs.isEmpty() && !prevAvgs.isEmpty()) {
            double recentMean = recentAvgs.stream().mapToDouble(d -> d).average().orElse(0);
            double prevMean   = prevAvgs.stream().mapToDouble(d -> d).average().orElse(0);
            if (recentMean - prevMean > 0.15) direction = "UP";
            else if (prevMean - recentMean > 0.15) direction = "DOWN";
        }

        // 3. Star distribution
        List<Object[]> distRows = ratingRepository.findStarDistributionByInstructorEmail(instructorEmail);
        Map<Integer, Long> dist = new TreeMap<>();
        for (int s = 1; s <= 5; s++) dist.put(s, 0L);
        for (Object[] row : distRows) {
            int stars = ((Number) row[0]).intValue();
            long cnt  = ((Number) row[1]).longValue();
            dist.put(stars, cnt);
        }

        // 4. Overall avg + total
        long totalRatings = dist.values().stream().mapToLong(v -> v).sum();
        double overallAvg = totalRatings == 0 ? 0.0 :
                dist.entrySet().stream().mapToDouble(e -> e.getKey() * e.getValue()).sum() / totalRatings;
        overallAvg = Math.round(overallAvg * 10.0) / 10.0;

        return RatingTrendDTO.builder()
                .overallAvg(overallAvg)
                .totalRatings(totalRatings)
                .trendDirection(direction)
                .monthlyTrend(monthly)
                .starDistribution(dist)
                .build();
    }

}