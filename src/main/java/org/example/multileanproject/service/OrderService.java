package org.example.multileanproject.service;

import org.example.multileanproject.dto.CheckoutPreviewResponseDTO;
import org.example.multileanproject.dto.OrderHistoryResponseDTO;
import org.example.multileanproject.entity.Order;

import java.util.List;

public interface OrderService {

    List<Order> getOrdersByStudentId(Long studentId);

    List<Order> getOrdersByCurrentUser(String email);

    List<OrderHistoryResponseDTO> getOrderHistoryByStudentId(Long studentId);

    List<OrderHistoryResponseDTO> getOrderHistoryByCurrentUser(String email);

    Order createOrderForCurrentUser(String email, Long courseId, String couponCode, String note);

    CheckoutPreviewResponseDTO previewCheckout(String email, Long courseId, String couponCode);

    void processPaymentSuccess(Long orderId);

    void processPaymentSuccess(Long orderId, String transactionRef, String paymentMethod);

    byte[] generateReceiptPdfByCurrentUser(String email, Long orderId);
}
