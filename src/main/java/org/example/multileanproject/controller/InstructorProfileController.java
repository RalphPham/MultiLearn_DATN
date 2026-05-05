package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.InstructorProfileDTO;
import org.example.multileanproject.entity.Instructor;
import org.example.multileanproject.repository.InstructorRepository;
import org.example.multileanproject.repository.OrderRepository;
import org.example.multileanproject.repository.WithdrawRequestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/instructor")
@RequiredArgsConstructor
public class InstructorProfileController {

    private final InstructorRepository instructorRepository;
    private final OrderRepository orderRepository;
    private final WithdrawRequestRepository withdrawRequestRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        return instructorRepository.findByUser_Email(auth.getName())
                .map(i -> ResponseEntity.ok(toDTO(i)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication auth,
                                           @RequestBody InstructorProfileDTO.UpdateRequest req) {
        Instructor instructor = instructorRepository.findByUser_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên"));

        if (req.getFullName() != null)    instructor.setFullName(req.getFullName());
        if (req.getPhone() != null)       instructor.setPhone(req.getPhone());
        if (req.getBio() != null)         instructor.setBio(req.getBio());
        if (req.getAvatarUrl() != null)   instructor.setAvatarUrl(req.getAvatarUrl());
        if (req.getBankName() != null)    instructor.setBankName(req.getBankName());
        if (req.getBankAccount() != null) instructor.setBankAccount(req.getBankAccount());

        instructorRepository.save(instructor);
        return ResponseEntity.ok(toDTO(instructor));
    }

    private InstructorProfileDTO toDTO(Instructor i) {
        // Tính walletBalance động: net earnings từ orders - approved withdrawals
        BigDecimal netEarnings = orderRepository.calculateTotalNetEarnings(i.getUser().getEmail());
        if (netEarnings == null) netEarnings = BigDecimal.ZERO;

        BigDecimal withdrawn = withdrawRequestRepository.sumApprovedWithdrawsByInstructorId(i.getId());
        if (withdrawn == null) withdrawn = BigDecimal.ZERO;

        BigDecimal realBalance = netEarnings.subtract(withdrawn).max(BigDecimal.ZERO);

        return InstructorProfileDTO.builder()
                .id(i.getId())
                .fullName(i.getFullName())
                .email(i.getUser().getEmail())
                .phone(i.getPhone())
                .bio(i.getBio())
                .avatarUrl(i.getAvatarUrl())
                .bankName(i.getBankName())
                .bankAccount(i.getBankAccount())
                .walletBalance(realBalance)
                .build();
    }
}
