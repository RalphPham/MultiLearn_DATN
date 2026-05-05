package org.example.multileanproject.entity;

public enum OrderStatus {
    PENDING,    // Chờ thanh toán
    COMPLETED,  // Đã thanh toán (Mới được tính để refund)
    CANCELLED,  // Đã hủy
    FAILED,     // Thất bại (Lỗi mạng, lỗi thẻ)
    REFUNDED    // <--- THÊM CÁI NÀY (Đã hoàn tiền)
}