package org.example.multileanproject.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.multileanproject.config.ConfigVNPay;
import org.example.multileanproject.dto.PaymentDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VnpayService {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public PaymentDTO.VNPayResponse createVnPayPayment(HttpServletRequest request, long amount, String bankCode, long orderId) {
        // 1. Chuyển đổi số tiền (VNPay yêu cầu nhân 100)
        long amountInCents = amount * 100;

        // SỬA LỖI 2: Đảm bảo mã giao dịch (vnp_TxnRef) không bao giờ bị trùng lặp khi test đi test lại
        String vnp_TxnRef = orderId + "_" + System.currentTimeMillis();
        String vnp_TmnCode = ConfigVNPay.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amountInCents));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);

        // Nội dung thanh toán (Không gõ dấu tiếng Việt)
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + vnp_TxnRef);

        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", frontendUrl + "/payment-result");

        // SỬA LỖI 3: Xử lý IP khi chạy localhost (tránh lỗi định dạng IPv6 của VNPay)
        String ipAddress = ConfigVNPay.getIpAddress(request);
        if (ipAddress != null && ipAddress.contains(":")) {
            ipAddress = "127.0.0.1";
        }
        vnp_Params.put("vnp_IpAddr", ipAddress);

        // Nếu người dùng chọn Ngân hàng cụ thể (Option)
        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }

        // SỬA LỖI 1: Chỉnh lại chuẩn múi giờ Việt Nam để không bị lỗi thời gian tạo giao dịch
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // --- BUILD URL ---
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        // Hash chuỗi dữ liệu
        String vnp_SecureHash = ConfigVNPay.hmacSHA512(ConfigVNPay.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        String paymentUrl = ConfigVNPay.vnp_PayUrl + "?" + queryUrl;

        return PaymentDTO.VNPayResponse.builder()
                .status("OK")
                .message("Successfully")
                .paymentUrl(paymentUrl)
                .build();
    }
}