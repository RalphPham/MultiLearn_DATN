package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.CheckoutOrderResponseDTO;
import org.example.multileanproject.dto.CheckoutPreviewRequestDTO;
import org.example.multileanproject.dto.CheckoutPreviewResponseDTO;
import org.example.multileanproject.dto.OrderHistoryResponseDTO;
import org.example.multileanproject.entity.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.example.multileanproject.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/me")
    public ResponseEntity<List<OrderHistoryResponseDTO>> getMyOrders() {
        String currentEmail = getRequiredEmail();
        return ResponseEntity.ok(orderService.getOrderHistoryByCurrentUser(currentEmail));
    }

    // Giữ lại để không bể URL cũ, nhưng bắt buộc phải là chính chủ
    @GetMapping("/user/{studentId}")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long studentId) {
        try {
            // Delegate về /me — bỏ qua studentId, dùng JWT để xác định user
            String currentEmail = getRequiredEmail();
            return ResponseEntity.ok(orderService.getOrderHistoryByCurrentUser(currentEmail));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String couponCode,
            @RequestParam(required = false) String note
    ) {
        try {
            String currentEmail = getRequiredEmail();
            Order order = orderService.createOrderForCurrentUser(currentEmail, courseId, couponCode, note);

            return ResponseEntity.ok(CheckoutOrderResponseDTO.fromEntity(order));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi thanh toán: " + e.getMessage());
        }
    }

    @PostMapping("/preview")
    public ResponseEntity<?> previewCheckout(@RequestBody CheckoutPreviewRequestDTO request) {
        try {
            String currentEmail = getRequiredEmail();

            CheckoutPreviewResponseDTO result = orderService.previewCheckout(
                    currentEmail,
                    request.getCourseId(),
                    request.getCouponCode()
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi preview thanh toán: " + e.getMessage());
        }
    }

    @GetMapping("/{orderId}/receipt")
    public ResponseEntity<?> downloadReceipt(@PathVariable Long orderId) {
        try {
            String currentEmail = getRequiredEmail();
            byte[] pdfBytes = orderService.generateReceiptPdfByCurrentUser(currentEmail, orderId);
            String fileName = "receipt_order_" + String.format("%04d", orderId) + ".pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String getRequiredEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new RuntimeException("Bạn chưa đăng nhập.");
        }

        if (!authentication.isAuthenticated()) {
            throw new RuntimeException("Phiên đăng nhập không hợp lệ.");
        }

        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("Bạn chưa đăng nhập.");
        }

        String email = authentication.getName();
        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            throw new RuntimeException("Không xác định được người dùng hiện tại.");
        }

        return email;
    }
}
