package org.example.multileanproject.repository;

import org.example.multileanproject.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrder_Id(Long orderId);
}
