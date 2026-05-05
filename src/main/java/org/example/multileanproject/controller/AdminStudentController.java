package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.multileanproject.dto.StudentStatusRequestDTO;
import org.example.multileanproject.entity.TicketStatus;
import org.example.multileanproject.repository.StudentStatusHistoryRepository;
import org.example.multileanproject.repository.SupportTicketRepository;
import org.example.multileanproject.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
@Slf4j
public class AdminStudentController {

    private final StudentService studentService;
    private final StudentStatusHistoryRepository historyRepository;
    private final SupportTicketRepository supportTicketRepository;

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','USER_LOCK')")
    public ResponseEntity<String> updateStudentStatus(
            @PathVariable Long id,
            @RequestBody StudentStatusRequestDTO request,
            Authentication authentication
    ) {
        String adminEmail = authentication != null ? authentication.getName() : "System Admin";
        studentService.updateStudentStatus(id, request, adminEmail);
        return ResponseEntity.ok(request.isActive()
                ? "Đã mở khóa tài khoản thành công!"
                : "Đã khóa tài khoản thành công!");
    }

    @GetMapping("/{id}/check-pending-ticket")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','USER_VIEW')")
    public ResponseEntity<?> checkPendingTicket(@PathVariable Long id) {
        boolean hasPending = supportTicketRepository.findByStudentIdOrderByIdDesc(id)
                .stream()
                .anyMatch(t -> t.getStatus() == TicketStatus.PENDING);
        return ResponseEntity.ok(Map.of("hasPending", hasPending));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','USER_VIEW')")
    public ResponseEntity<?> getStudentHistory(@PathVariable Long id) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        List<Map<String, String>> historyData = historyRepository.findByStudentIdOrderByIdDesc(id)
                .stream()
                .map(h -> Map.of(
                        "date", h.getCreatedAt() != null ? h.getCreatedAt().format(formatter) : "N/A",
                        "action", h.getNewStatus(),
                        "reason", h.getReason() != null ? h.getReason() : "",
                        "adminName", h.getActionBy() != null ? h.getActionBy() : "Admin"
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(historyData);
    }
}
