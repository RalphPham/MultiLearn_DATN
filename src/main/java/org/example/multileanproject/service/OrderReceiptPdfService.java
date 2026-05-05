package org.example.multileanproject.service;

import org.example.multileanproject.entity.Order;
import org.example.multileanproject.entity.RefundRequest;

public interface OrderReceiptPdfService {

    byte[] generateOrderReceipt(Order order, RefundRequest approvedRefund);
}
