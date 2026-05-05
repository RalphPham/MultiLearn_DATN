package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.StudentStatusRequestDTO;
import org.example.multileanproject.dto.TicketRequestDTO;
import org.example.multileanproject.dto.TicketResponseDTO;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.entity.SupportTicket;
import org.example.multileanproject.entity.TicketStatus;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final StudentRepository studentRepository;
    private final EmailService emailService;
    private final StudentService studentService;

    public SupportTicket createTicket(String email, TicketRequestDTO.Create request) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Khong tim thay user"));

        SupportTicket ticket = SupportTicket.builder()
                .student(student)
                .title(request.getTitle())
                .content(request.getContent())
                .status(TicketStatus.PENDING)
                .build();
        return ticketRepository.save(ticket);
    }

    public List<SupportTicket> getMyTickets(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Khong tim thay user"));
        return ticketRepository.findByStudentIdOrderByIdDesc(student.getId());
    }

    public List<TicketResponseDTO> getAllTickets() {
        return ticketRepository.findAllByOrderByIdDesc().stream().map(t ->
                TicketResponseDTO.builder()
                        .id(t.getId())
                        .title(t.getTitle())
                        .content(t.getContent())
                        .adminReply(t.getAdminReply())
                        .status(t.getStatus().name())
                        .createdAt(t.getCreatedAt())
                        .updatedAt(t.getUpdatedAt())
                        .student(TicketResponseDTO.StudentInfo.builder()
                                .id(t.getStudent().getId())
                                .fullName(t.getStudent().getFullName())
                                .email(t.getStudent().getEmail())
                                .build())
                        .build()
        ).collect(Collectors.toList());
    }

    public SupportTicket replyTicket(Long ticketId, TicketRequestDTO.Reply request) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay don khieu nai"));

        ticket.setAdminReply(request.getAdminReply());
        ticket.setStatus(request.getStatus());

        Student student = ticket.getStudent();
        if (request.getStatus() == TicketStatus.APPROVED) {
            StudentStatusRequestDTO statusRequest = new StudentStatusRequestDTO();
            statusRequest.setActive(true);
            statusRequest.setReason("Mo khoa qua khieu nai: " + safe(request.getAdminReply()));
            statusRequest.setSource("support_ticket");
            studentService.updateStudentStatus(student.getId(), statusRequest, "Admin (duyet khieu nai)");
        }

        try {
            if (request.getStatus() != TicketStatus.APPROVED) {
                emailService.sendAccountLockedEmail(
                        student.getEmail(),
                        student.getFullName(),
                        "Yeu cau mo khoa bi tu choi. Phan quyet: " + safe(request.getAdminReply())
                );
            }
        } catch (Exception e) {
            System.err.println("Loi gui email: " + e.getMessage());
        }

        return ticketRepository.save(ticket);
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }
}

