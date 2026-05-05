package org.example.multileanproject.repository;

import org.example.multileanproject.entity.SupportTicket;
import org.example.multileanproject.entity.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    Page<SupportTicket> findByStatus(TicketStatus status, Pageable pageable);
    List<SupportTicket> findByStudentIdOrderByIdDesc(Long studentId);
    List<SupportTicket> findByStudent_EmailAndStatus(String email, TicketStatus status);
    List<SupportTicket> findAllByOrderByIdDesc();
}
