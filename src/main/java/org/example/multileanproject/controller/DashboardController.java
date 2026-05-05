package org.example.multileanproject.controller;

import org.example.multileanproject.dto.ChartDTO;
import org.example.multileanproject.dto.DashboardStatsDTO;
import org.example.multileanproject.dto.TopCourseDTO;
import org.example.multileanproject.dto.TopInstructorDTO;
import org.example.multileanproject.entity.CourseStatus;
import org.example.multileanproject.entity.Instructor;
import org.example.multileanproject.entity.Order;
import org.example.multileanproject.entity.OrderStatus;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.InstructorRepository;
import org.example.multileanproject.repository.OrderRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN','DASHBOARD_VIEW','STATS_VIEW')")
public class DashboardController {

    private final StudentRepository studentRepository;
    private final OrderRepository orderRepository;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;

    public DashboardController(StudentRepository studentRepository,
                               OrderRepository orderRepository,
                               CourseRepository courseRepository,
                               InstructorRepository instructorRepository) {
        this.studentRepository = studentRepository;
        this.orderRepository = orderRepository;
        this.courseRepository = courseRepository;
        this.instructorRepository = instructorRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        ZoneId vn = ZoneId.of("Asia/Ho_Chi_Minh");

        if (from != null && to != null) {
            LocalDateTime dtFrom = from.atStartOfDay();
            LocalDateTime dtTo   = to.atTime(LocalTime.MAX);

            BigDecimal revenue = orderRepository.sumRevenueByStatusAndRange(OrderStatus.COMPLETED, dtFrom, dtTo);
            if (revenue == null) revenue = BigDecimal.ZERO;

            BigDecimal instructorPayout = orderRepository.calculateTotalInstructorPayoutInRange(dtFrom, dtTo);
            if (instructorPayout == null) instructorPayout = BigDecimal.ZERO;
            BigDecimal platformRevenue = revenue.subtract(instructorPayout).max(BigDecimal.ZERO);

            long newOrders      = orderRepository.countByStatusAndRange(OrderStatus.PENDING,    dtFrom, dtTo);
            long completed      = orderRepository.countByStatusAndRange(OrderStatus.COMPLETED,  dtFrom, dtTo);
            long cancelled      = orderRepository.countByStatusAndRange(OrderStatus.CANCELLED,  dtFrom, dtTo);
            long refunded       = orderRepository.countByStatusAndRange(OrderStatus.REFUNDED,   dtFrom, dtTo);
            long pendingCourses = courseRepository.countByStatus(CourseStatus.PENDING_APPROVAL);
            long totalStudents  = studentRepository.countStudentsBetween(dtFrom, dtTo);
            long totalCourses   = courseRepository.countByStatusAndDateRange(CourseStatus.PUBLISHED, dtFrom, dtTo);

            long denominator = completed + cancelled + refunded;
            double completionRate = denominator == 0 ? 0.0
                    : BigDecimal.valueOf(completed * 100.0 / denominator)
                                .setScale(1, RoundingMode.HALF_UP)
                                .doubleValue();

            return ResponseEntity.ok(DashboardStatsDTO.builder()
                    .totalStudents(totalStudents)
                    .totalCourses(totalCourses)
                    .totalRevenue(revenue)
                    .instructorPayout(instructorPayout)
                    .platformRevenue(platformRevenue)
                    .pendingCourses(pendingCourses)
                    .newOrders(newOrders)
                    .completedOrders(completed)
                    .cancelledOrders(cancelled)
                    .refundedOrders(refunded)
                    .newStudentsThisMonth(0)
                    .completionRate(completionRate)
                    .build());
        }

        BigDecimal revenue = orderRepository.sumRevenueByStatus(OrderStatus.COMPLETED);
        if (revenue == null) revenue = BigDecimal.ZERO;

        BigDecimal instructorPayout = orderRepository.calculateTotalInstructorPayout();
        if (instructorPayout == null) instructorPayout = BigDecimal.ZERO;
        BigDecimal platformRevenue = revenue.subtract(instructorPayout).max(BigDecimal.ZERO);

        long newOrders      = orderRepository.countByStatus(OrderStatus.PENDING);
        long completed      = orderRepository.countByStatus(OrderStatus.COMPLETED);
        long cancelled      = orderRepository.countByStatus(OrderStatus.CANCELLED);
        long refunded       = orderRepository.countByStatus(OrderStatus.REFUNDED);
        long pendingCourses = courseRepository.countByStatus(CourseStatus.PENDING_APPROVAL);
        long totalStudents  = studentRepository.count();
        long totalCourses   = courseRepository.count();

        LocalDateTime firstDayOfMonth = LocalDate.now(vn).withDayOfMonth(1).atStartOfDay();
        long newStudentsThisMonth = studentRepository.countNewStudentsSince(firstDayOfMonth);

        long denominator = completed + cancelled + refunded;
        double completionRate = denominator == 0 ? 0.0
                : BigDecimal.valueOf(completed * 100.0 / denominator)
                            .setScale(1, RoundingMode.HALF_UP)
                            .doubleValue();

        return ResponseEntity.ok(DashboardStatsDTO.builder()
                .totalStudents(totalStudents)
                .totalCourses(totalCourses)
                .totalRevenue(revenue)
                .instructorPayout(instructorPayout)
                .platformRevenue(platformRevenue)
                .pendingCourses(pendingCourses)
                .newOrders(newOrders)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .refundedOrders(refunded)
                .newStudentsThisMonth(newStudentsThisMonth)
                .completionRate(completionRate)
                .build());
    }

    @GetMapping("/order-status")
    public ResponseEntity<Map<String, Long>> getOrderStatusCounts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Map<String, Long> result = new LinkedHashMap<>();
        if (from != null && to != null) {
            LocalDateTime dtFrom = from.atStartOfDay();
            LocalDateTime dtTo   = to.atTime(LocalTime.MAX);
            result.put("COMPLETED", orderRepository.countByStatusAndRange(OrderStatus.COMPLETED, dtFrom, dtTo));
            result.put("PENDING",   orderRepository.countByStatusAndRange(OrderStatus.PENDING,   dtFrom, dtTo));
            result.put("CANCELLED", orderRepository.countByStatusAndRange(OrderStatus.CANCELLED, dtFrom, dtTo));
            result.put("REFUNDED",  orderRepository.countByStatusAndRange(OrderStatus.REFUNDED,  dtFrom, dtTo));
        } else {
            result.put("COMPLETED", orderRepository.countByStatus(OrderStatus.COMPLETED));
            result.put("PENDING",   orderRepository.countByStatus(OrderStatus.PENDING));
            result.put("CANCELLED", orderRepository.countByStatus(OrderStatus.CANCELLED));
            result.put("REFUNDED",  orderRepository.countByStatus(OrderStatus.REFUNDED));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/chart")
    public ResponseEntity<List<ChartDTO>> getRevenueChart(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long instructorId
    ) {
        ZoneId vn = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate endDate   = (to   != null) ? to   : LocalDate.now(vn);
        LocalDate startDate = (from != null) ? from : endDate.minusDays(6);

        Map<LocalDate, BigDecimal> revenueMap = new LinkedHashMap<>();
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        for (int i = 0; i <= daysBetween; i++) {
            revenueMap.put(startDate.plusDays(i), BigDecimal.ZERO);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        if (instructorId != null) {
            List<Object[]> rows = orderRepository.findDailyRevenueByInstructor(
                    instructorId,
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX)
            );
            for (Object[] row : rows) {
                LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
                BigDecimal rev = new BigDecimal(row[1].toString());
                if (revenueMap.containsKey(date)) revenueMap.put(date, rev);
            }
        } else {
            List<Order> orders = orderRepository.findByStatusAndCreatedAtBetween(
                    OrderStatus.COMPLETED,
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX)
            );
            for (Order order : orders) {
                LocalDate date = order.getCreatedAt().toLocalDate();
                if (revenueMap.containsKey(date)) {
                    revenueMap.put(date, revenueMap.get(date).add(order.getFinalAmount()));
                }
            }
        }

        List<ChartDTO> result = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : revenueMap.entrySet()) {
            result.add(new ChartDTO(entry.getKey().format(formatter), entry.getValue()));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/chart/monthly")
    public ResponseEntity<List<ChartDTO>> getMonthlyRevenueChart(
            @RequestParam(defaultValue = "12") int months,
            @RequestParam(required = false) Integer year
    ) {
        int safeMonths;
        YearMonth startMonth;
        YearMonth endMonth;

        if (year != null) {
            safeMonths = 12;
            startMonth = YearMonth.of(year, 1);
            endMonth = YearMonth.of(year, 12);
        } else {
            ZoneId vn = ZoneId.of("Asia/Ho_Chi_Minh");
            safeMonths = Math.max(1, Math.min(months, 36));
            endMonth = YearMonth.now(vn);
            startMonth = endMonth.minusMonths(safeMonths - 1L);
        }

        LocalDateTime start = startMonth.atDay(1).atStartOfDay();
        LocalDateTime end = endMonth.atEndOfMonth().atTime(LocalTime.MAX);

        // 3 chỉ số mỗi tháng: total / instructor / platform
        Map<YearMonth, BigDecimal[]> monthMap = new LinkedHashMap<>();
        for (int i = 0; i < safeMonths; i++) {
            monthMap.put(startMonth.plusMonths(i),
                    new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
        }

        List<Object[]> rows = orderRepository.findMonthlyRevenueBreakdown(start, end);
        for (Object[] row : rows) {
            int yyyy = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal total = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
            BigDecimal instructor = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
            BigDecimal platform = row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO;
            YearMonth ym = YearMonth.of(yyyy, month);
            if (monthMap.containsKey(ym)) {
                monthMap.put(ym, new BigDecimal[]{total, platform, instructor});
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        List<ChartDTO> result = new ArrayList<>();
        for (Map.Entry<YearMonth, BigDecimal[]> entry : monthMap.entrySet()) {
            BigDecimal[] v = entry.getValue();
            result.add(new ChartDTO(entry.getKey().format(formatter), v[0], v[1], v[2]));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/instructors-list")
    public ResponseEntity<List<Map<String, Object>>> getInstructorsList() {
        List<Instructor> instructors = instructorRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Instructor ins : instructors) {
            if (ins.getUser() != null) {
                result.add(Map.of(
                        "id",       ins.getUser().getId(),
                        "fullName", ins.getUser().getFullName() != null ? ins.getUser().getFullName() : ins.getUser().getEmail()
                ));
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/top-courses")
    public ResponseEntity<List<TopCourseDTO>> getTopCourses(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<Object[]> rows;
        if (from != null || to != null) {
            LocalDateTime dtFrom = from != null ? from.atStartOfDay() : null;
            LocalDateTime dtTo   = to   != null ? to.atTime(LocalTime.MAX) : null;
            rows = orderRepository.findTopCoursesByRevenueInRange(dtFrom, dtTo, PageRequest.of(0, limit));
        } else {
            rows = orderRepository.findTopCoursesByRevenue(PageRequest.of(0, limit));
        }
        List<TopCourseDTO> result = new ArrayList<>();
        for (Object[] r : rows) {
            result.add(new TopCourseDTO(
                    ((Number) r[0]).longValue(),
                    (String)  r[1],
                    (String)  r[2],
                    ((Number) r[3]).longValue(),
                    r[4] != null ? new BigDecimal(r[4].toString()) : BigDecimal.ZERO
            ));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/top-instructors")
    public ResponseEntity<List<TopInstructorDTO>> getTopInstructors(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<Object[]> rows;
        if (from != null || to != null) {
            LocalDateTime dtFrom = from != null ? from.atStartOfDay() : null;
            LocalDateTime dtTo   = to   != null ? to.atTime(LocalTime.MAX) : null;
            rows = orderRepository.findTopInstructorsByRevenueInRange(dtFrom, dtTo, PageRequest.of(0, limit));
        } else {
            rows = orderRepository.findTopInstructorsByRevenue(PageRequest.of(0, limit));
        }
        List<TopInstructorDTO> result = new ArrayList<>();
        for (Object[] r : rows) {
            result.add(new TopInstructorDTO(
                    ((Number) r[0]).longValue(),
                    (String)  r[1],
                    (String)  r[2],
                    ((Number) r[3]).longValue(),
                    r[4] != null ? new BigDecimal(r[4].toString()) : BigDecimal.ZERO
            ));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/new-students-chart")
    public ResponseEntity<List<ChartDTO>> getNewStudentsChart(
            @RequestParam(required = false) Integer year
    ) {
        ZoneId vn = ZoneId.of("Asia/Ho_Chi_Minh");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");

        YearMonth startMonth;
        YearMonth endMonth;
        LocalDateTime since;
        List<Object[]> rows;

        if (year != null) {
            startMonth = YearMonth.of(year, 1);
            endMonth = YearMonth.of(year, 12);
            since = startMonth.atDay(1).atStartOfDay();
            LocalDateTime until = endMonth.atEndOfMonth().atTime(LocalTime.MAX);
            rows = studentRepository.countNewStudentsByMonthBetween(since, until);
        } else {
            endMonth = YearMonth.now(vn);
            startMonth = endMonth.minusMonths(5);
            since = startMonth.atDay(1).atStartOfDay();
            rows = studentRepository.countNewStudentsByMonth(since);
        }

        Map<String, Long> monthMap = new LinkedHashMap<>();
        YearMonth cursor = startMonth;
        while (!cursor.isAfter(endMonth)) {
            monthMap.put(cursor.format(fmt), 0L);
            cursor = cursor.plusMonths(1);
        }

        for (Object[] r : rows) {
            int yyyy = ((Number) r[0]).intValue();
            int mm = ((Number) r[1]).intValue();
            long count = ((Number) r[2]).longValue();
            String key = String.format("%02d/%04d", mm, yyyy);
            if (monthMap.containsKey(key)) monthMap.put(key, count);
        }

        List<ChartDTO> result = new ArrayList<>();
        for (Map.Entry<String, Long> e : monthMap.entrySet()) {
            result.add(new ChartDTO(e.getKey(), BigDecimal.valueOf(e.getValue())));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/approved-courses-chart")
    public ResponseEntity<List<ChartDTO>> getApprovedCoursesChart(
            @RequestParam(required = false) Integer year
    ) {
        ZoneId vn = ZoneId.of("Asia/Ho_Chi_Minh");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");

        YearMonth startMonth;
        YearMonth endMonth;

        if (year != null) {
            startMonth = YearMonth.of(year, 1);
            endMonth = YearMonth.of(year, 12);
        } else {
            endMonth = YearMonth.now(vn);
            startMonth = endMonth.minusMonths(5);
        }

        LocalDateTime start = startMonth.atDay(1).atStartOfDay();
        LocalDateTime end = endMonth.atEndOfMonth().atTime(LocalTime.MAX);

        Map<String, Long> monthMap = new LinkedHashMap<>();
        YearMonth cursor = startMonth;
        while (!cursor.isAfter(endMonth)) {
            monthMap.put(cursor.format(fmt), 0L);
            cursor = cursor.plusMonths(1);
        }

        List<Object[]> rows = courseRepository.countPublishedCoursesByMonth(start, end);
        for (Object[] row : rows) {
            int yyyy = ((Number) row[0]).intValue();
            int mm = ((Number) row[1]).intValue();
            long total = ((Number) row[2]).longValue();
            String key = String.format("%02d/%04d", mm, yyyy);
            if (monthMap.containsKey(key)) monthMap.put(key, total);
        }

        List<ChartDTO> result = new ArrayList<>();
        for (Map.Entry<String, Long> e : monthMap.entrySet()) {
            result.add(new ChartDTO(e.getKey(), BigDecimal.valueOf(e.getValue())));
        }
        return ResponseEntity.ok(result);
    }
}
