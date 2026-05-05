package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.TicketRequestDTO;
import org.example.multileanproject.service.SupportTicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService ticketService;

    @GetMapping("/students/tickets")
    public ResponseEntity<?> getMyTickets(Authentication auth) {
        return ResponseEntity.ok(ticketService.getMyTickets(auth.getName()));
    }

    @GetMapping("/admin/tickets")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','TICKET_VIEW')")
    public ResponseEntity<?> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @PutMapping("/admin/tickets/{id}/reply")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','TICKET_REPLY')")
    public ResponseEntity<?> replyTicket(@PathVariable Long id, @RequestBody TicketRequestDTO.Reply request) {
        return ResponseEntity.ok(ticketService.replyTicket(id, request));
    }
}
