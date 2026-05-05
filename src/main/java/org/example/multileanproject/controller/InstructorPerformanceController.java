package org.example.multileanproject.controller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.InstructorRevenueSummaryDTO;
import org.example.multileanproject.dto.InstructorStudentItemDTO;
import org.example.multileanproject.dto.InstructorStudentOverviewDTO;
import org.example.multileanproject.dto.LessonDropoffDTO;
import org.example.multileanproject.dto.RatingTrendDTO;
import org.example.multileanproject.service.InstructorPerformanceService;
import java.time.LocalDate;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instructor/performance")
@RequiredArgsConstructor
public class InstructorPerformanceController {

    private final InstructorPerformanceService instructorPerformanceService;

    @GetMapping("/student-overview")
    public ResponseEntity<?> getStudentOverview(Authentication authentication) {
        try {
            if (authentication == null
                    || authentication.getName() == null
                    || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(401).body("Bạn chưa đăng nhập.");
            }

            InstructorStudentOverviewDTO result =
                    instructorPerformanceService.getStudentOverview(authentication.getName());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Lỗi lấy thống kê học viên: " + e.getMessage());
        }
    }
    @GetMapping("/students")
    public ResponseEntity<?> getStudentItems(Authentication authentication) {
        try {
            if (authentication == null
                    || authentication.getName() == null
                    || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(401).body("Bạn chưa đăng nhập.");
            }

            return ResponseEntity.ok(
                    instructorPerformanceService.getStudentItems(authentication.getName())
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Lỗi lấy danh sách học viên: " + e.getMessage());
        }
    }
    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenue(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Authentication authentication) {

        if (authentication == null || authentication.getName().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String email = authentication.getName();

        InstructorRevenueSummaryDTO result =
                instructorPerformanceService    .getRevenueSummary(email, year, fromDate, toDate);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/at-risk")
    public ResponseEntity<?> getAtRiskStudents(Authentication authentication) {
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            return ResponseEntity.ok(
                    instructorPerformanceService.getAtRiskStudents(authentication.getName()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/course/{courseId}/dropoff")
    public ResponseEntity<?> getLessonDropoff(
            @PathVariable Long courseId,
            Authentication authentication) {
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            List<LessonDropoffDTO> data =
                    instructorPerformanceService.getLessonDropoff(authentication.getName(), courseId);
            return ResponseEntity.ok(data);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/rating-trend")
    public ResponseEntity<?> getRatingTrend(Authentication authentication) {
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            RatingTrendDTO data = instructorPerformanceService.getRatingTrend(authentication.getName());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/students/export")
    public ResponseEntity<?> exportStudents(Authentication authentication) {
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        try {
            List<InstructorStudentItemDTO> rows =
                    instructorPerformanceService.getStudentItems(authentication.getName());

            byte[] data = buildStudentsWorkbook(rows);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"instructor-students.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Lỗi xuất Excel học viên: " + e.getMessage());
        }
    }

    private byte[] buildStudentsWorkbook(List<InstructorStudentItemDTO> rows) throws Exception {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Students");
            sheet.setDefaultColumnWidth(22);

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {
                    "STT", "Học viên", "Email", "Khóa học", "Slug khóa học",
                    "Trạng thái", "Tiến độ (%)", "Ngày ghi danh", "Avatar học viên", "Ảnh khóa học"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (InstructorStudentItemDTO item : rows) {
                Row row = sheet.createRow(rowNum);
                row.createCell(0).setCellValue(rowNum);
                row.createCell(1).setCellValue(safe(item.getStudentName()));
                row.createCell(2).setCellValue(safe(item.getStudentEmail()));
                row.createCell(3).setCellValue(safe(item.getCourseTitle()));
                row.createCell(4).setCellValue(safe(item.getCourseSlug()));
                row.createCell(5).setCellValue(safe(item.getEnrollmentStatus()));
                row.createCell(6).setCellValue(item.getProgress() != null ? item.getProgress() : 0.0);
                row.createCell(7).setCellValue(item.getEnrolledAt() != null ? item.getEnrolledAt().format(dtf) : "");
                row.createCell(8).setCellValue(safe(item.getStudentAvatar()));
                row.createCell(9).setCellValue(safe(item.getCourseThumbnail()));
                rowNum++;
            }

            sheet.setColumnWidth(1, 6000);
            sheet.setColumnWidth(2, 8000);
            sheet.setColumnWidth(3, 9000);
            sheet.setColumnWidth(4, 7000);
            sheet.setColumnWidth(8, 10000);
            sheet.setColumnWidth(9, 10000);

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                wb.write(out);
                return out.toByteArray();
            }
        }
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}
