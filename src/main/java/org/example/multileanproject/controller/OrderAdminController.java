package org.example.multileanproject.controller;

import org.example.multileanproject.dto.OrderDTO;
import org.example.multileanproject.service.OrderAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
public class OrderAdminController {
    private final OrderAdminService orderAdminService;

    public OrderAdminController(OrderAdminService orderAdminService) {
        this.orderAdminService = orderAdminService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ORDER_VIEW')")
    public ResponseEntity<Page<OrderDTO>> getOrders(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(orderAdminService.getAllOrders(q, status, pageable));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ORDER_UPDATE')")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        orderAdminService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ORDER_EXPORT')")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status) {
        try {
            byte[] data = orderAdminService.exportToExcel(q, status);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"orders.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
