package org.example.multileanproject.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.multileanproject.dto.OrderDTO;
import org.example.multileanproject.entity.Order;
import org.example.multileanproject.entity.OrderDetail;
import org.example.multileanproject.entity.OrderStatus;
import org.example.multileanproject.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class OrderAdminService {
    private final OrderRepository orderRepository;
    private final AdminActionLogService adminActionLogService;
    private final OrderService orderService;

    public OrderAdminService(OrderRepository orderRepository,
                             AdminActionLogService adminActionLogService,
                             OrderService orderService) {
        this.orderRepository = orderRepository;
        this.adminActionLogService = adminActionLogService;
        this.orderService = orderService;
    }

    // 1. Lấy danh sách đơn hàng
    public Page<OrderDTO> getAllOrders(String keyword, String statusStr, Pageable pageable) {
        OrderStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try { status = OrderStatus.valueOf(statusStr); } catch (Exception e) {}
        }

        return orderRepository.searchOrders(keyword, status, pageable)
                .map(this::convertToDTO);
    }

    // 2. Duyệt / Hủy đơn hàng
    @Transactional
    public void updateStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        final OrderStatus currentStatus = order.getStatus();
        final OrderStatus targetStatus;
        try {
            targetStatus = OrderStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + newStatus);
        }

        if (currentStatus == targetStatus) {
            return;
        }

        if (currentStatus == OrderStatus.COMPLETED && targetStatus != OrderStatus.COMPLETED) {
            throw new RuntimeException("Đơn hàng đã hoàn thành, không thể chuyển trạng thái trực tiếp.");
        }

        if (targetStatus == OrderStatus.COMPLETED) {
            // Luôn đi qua nghiệp vụ chuẩn để kích hoạt enrollment + notification + cleanup duplicate pending orders.
            orderService.processPaymentSuccess(orderId);
        } else {
            order.setStatus(targetStatus);
            orderRepository.save(order);
        }

        adminActionLogService.log("UPDATE_ORDER_STATUS",
                "Cập nhật đơn hàng #" + orderId + " → " + targetStatus.name(), orderId, "ORDER");
    }

    public byte[] exportToExcel(String keyword, String statusStr) throws Exception {
        OrderStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try { status = OrderStatus.valueOf(statusStr); } catch (Exception ignored) {}
        }
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        List<OrderDTO> orders = orderRepository.searchOrdersAll(kw, status)
                .stream().map(this::convertToDTO).collect(Collectors.toList());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        NumberFormat vnd = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Đơn hàng");
            sheet.setDefaultColumnWidth(20);

            // ── Style: header ──────────────────────────────────────────────────
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // ── Style: row thường ─────────────────────────────────────────────
            CellStyle rowStyle = wb.createCellStyle();
            rowStyle.setBorderBottom(BorderStyle.THIN);
            rowStyle.setBorderLeft(BorderStyle.THIN);
            rowStyle.setBorderRight(BorderStyle.THIN);
            rowStyle.setWrapText(true);

            CellStyle altStyle = wb.createCellStyle();
            altStyle.cloneStyleFrom(rowStyle);
            altStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ── Tiêu đề bảng ──────────────────────────────────────────────────
            String[] headers = { "Mã ĐH", "Học viên", "Email", "Khóa học",
                    "Giá gốc", "Giảm giá", "Mã voucher", "Thanh toán cuối",
                    "Trạng thái", "Thanh toán", "Ngày tạo" };
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(28);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Dữ liệu ───────────────────────────────────────────────────────
            int rowNum = 1;
            for (OrderDTO o : orders) {
                Row row = sheet.createRow(rowNum);
                CellStyle style = (rowNum % 2 == 0) ? altStyle : rowStyle;

                row.createCell(0).setCellValue(o.getId() != null ? o.getId() : 0);
                row.createCell(1).setCellValue(o.getStudentName() != null ? o.getStudentName() : "");
                row.createCell(2).setCellValue(o.getStudentEmail() != null ? o.getStudentEmail() : "");
                String courses = o.getCourseNames() != null ? String.join(", ", o.getCourseNames()) : "";
                row.createCell(3).setCellValue(courses);
                row.createCell(4).setCellValue(o.getOriginalAmount() != null ? vnd.format(o.getOriginalAmount()) : "0");
                row.createCell(5).setCellValue(o.getDiscountAmount() != null ? vnd.format(o.getDiscountAmount()) : "0");
                row.createCell(6).setCellValue(o.getCouponCode() != null ? o.getCouponCode() : "");
                row.createCell(7).setCellValue(o.getFinalAmount() != null ? vnd.format(o.getFinalAmount()) : "0");
                row.createCell(8).setCellValue(translateStatus(o.getStatus()));
                row.createCell(9).setCellValue(o.getPaymentMethod() != null ? o.getPaymentMethod() : "");
                row.createCell(10).setCellValue(o.getCreatedAt() != null ? o.getCreatedAt().format(dtf) : "");

                for (int c = 0; c < 11; c++) {
                    if (row.getCell(c) != null) row.getCell(c).setCellStyle(style);
                }
                rowNum++;
            }

            // Auto-size các cột chính
            sheet.setColumnWidth(1, 6000);
            sheet.setColumnWidth(2, 7000);
            sheet.setColumnWidth(3, 12000);
            sheet.setColumnWidth(4, 5000);
            sheet.setColumnWidth(5, 5000);
            sheet.setColumnWidth(6, 4000);
            sheet.setColumnWidth(7, 5000);
            sheet.setColumnWidth(10, 5500);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    private String translateStatus(String status) {
        if (status == null) return "";
        return switch (status) {
            case "PENDING"   -> "Chờ xử lý";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            case "FAILED"    -> "Thất bại";
            default          -> status;
        };
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        BigDecimal original = order.getOriginalAmount() != null ? order.getOriginalAmount() : BigDecimal.ZERO;
        BigDecimal finalAmt  = order.getFinalAmount()   != null ? order.getFinalAmount()   : BigDecimal.ZERO;
        dto.setOriginalAmount(original);
        dto.setFinalAmount(finalAmt);
        dto.setDiscountAmount(original.subtract(finalAmt).max(BigDecimal.ZERO));
        dto.setCouponCode(order.getCouponCode());
        dto.setStatus(order.getStatus().name());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCreatedAt(order.getCreatedAt());

        if (order.getStudent() != null) {
            dto.setStudentName(order.getStudent().getFullName());
            dto.setStudentEmail(order.getStudent().getEmail());
        }

        // Lấy tên các khóa học trong đơn hàng
        if (order.getOrderItems() != null) {
            List<String> courseNames = order.getOrderItems().stream()
                    .filter(item -> item.getCourse() != null)
                    .map(item -> item.getCourse().getTitle())
                    .collect(Collectors.toList());
            dto.setCourseNames(courseNames);
        }

        return dto;
    }
}
