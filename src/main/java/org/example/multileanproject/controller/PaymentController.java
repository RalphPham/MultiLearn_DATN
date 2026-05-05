package org.example.multileanproject.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.multileanproject.config.ConfigVNPay;
import org.example.multileanproject.dto.PaymentDTO;
import org.example.multileanproject.service.OrderService;
import org.example.multileanproject.service.VnpayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VnpayService vnpayService; // Inject Service thanh toán
    private final OrderService orderService; // <--- QUAN TRỌNG: Inject cái này để sửa lỗi dòng 40

    // 1. Tạo URL thanh toán
    @GetMapping("/create-payment")
    public ResponseEntity<?> createPayment(
            HttpServletRequest request,
            @RequestParam Long orderId,
            @RequestParam long amount // Số tiền (VND)
    ) {
        // Gọi vnpayService (đã đổi tên từ PaymentService)
        PaymentDTO.VNPayResponse response = vnpayService.createVnPayPayment(request, amount, "NCB", orderId);
        return ResponseEntity.ok(response);
    }

    // 2. Xử lý kết quả trả về từ VNPay
    @GetMapping("/vnpay-callback")
    public ResponseEntity<?> paymentCallback(HttpServletRequest request) {
        try {
            Map<String, String[]> params = request.getParameterMap();

            // Lấy chữ ký VNPay gửi về
            String vnpSecureHash = request.getParameter("vnp_SecureHash");
            if (vnpSecureHash == null) {
                return ResponseEntity.badRequest().body("Thiếu chữ ký vnp_SecureHash");
            }

            // Tạo chuỗi hash từ tất cả params (trừ vnp_SecureHash, vnp_SecureHashType) — sắp xếp theo alphabet
            TreeMap<String, String> sortedParams = new TreeMap<>();
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                String key = entry.getKey();
                if (!key.equals("vnp_SecureHash") && !key.equals("vnp_SecureHashType")) {
                    sortedParams.put(key, entry.getValue()[0]);
                }
            }

            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                String value = entry.getValue();
                if (value != null && !value.isEmpty()) {
                    if (hashData.length() > 0) hashData.append("&");
                    hashData.append(entry.getKey()).append("=")
                            .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                }
            }

            String computedHash = ConfigVNPay.hmacSHA512(ConfigVNPay.vnp_HashSecret, hashData.toString());

            // So sánh chữ ký — nếu không khớp thì có người đang giả mạo callback
            if (!computedHash.equalsIgnoreCase(vnpSecureHash)) {
                return ResponseEntity.badRequest().body("Chữ ký không hợp lệ");
            }

            // Chữ ký hợp lệ — tiếp tục xử lý
            String responseCode = request.getParameter("vnp_ResponseCode");
            String vnpTxnRef = request.getParameter("vnp_TxnRef");
            String vnpTransactionNo = request.getParameter("vnp_TransactionNo");
            String vnpBankCode = request.getParameter("vnp_BankCode");

            if (vnpTxnRef == null || vnpTxnRef.isBlank()) {
                return ResponseEntity.badRequest().body("Thiếu vnp_TxnRef");
            }
            String[] parts = vnpTxnRef.split("_");
            if (parts.length == 0) {
                return ResponseEntity.badRequest().body("vnp_TxnRef không hợp lệ: " + vnpTxnRef);
            }
            Long realOrderId;
            try {
                realOrderId = Long.parseLong(parts[0]);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body("Không thể đọc mã đơn hàng từ vnp_TxnRef: " + vnpTxnRef);
            }

            if ("00".equals(responseCode)) {
                String transactionRef = (vnpTransactionNo != null && !vnpTransactionNo.isBlank())
                        ? vnpTransactionNo
                        : vnpTxnRef;
                String paymentMethod = (vnpBankCode != null && !vnpBankCode.isBlank())
                        ? ("VNPAY-" + vnpBankCode)
                        : "VNPAY";
                orderService.processPaymentSuccess(realOrderId, transactionRef, paymentMethod);
                return ResponseEntity.ok("Giao dịch thành công");
            } else {
                return ResponseEntity.badRequest().body("Giao dịch thất bại tại cổng VNPay");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xử lý callback: " + e.getMessage());
        }
    }
}
