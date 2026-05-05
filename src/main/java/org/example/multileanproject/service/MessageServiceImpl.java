package org.example.multileanproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.ContactDTO;
import org.example.multileanproject.entity.Instructor;
import org.example.multileanproject.entity.Message;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.repository.InstructorRepository;
import org.example.multileanproject.repository.MessageRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.service.MessageService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;

    @Override
    public List<ContactDTO> getContactsForInstructor(String email) {

        Instructor instructor = getCurrentInstructor(email);

        return messageRepository.findContactsForInstructor(instructor.getId());
    }

    @Override
    public List<Message> getChatHistory(String email, Long studentId) {

        Instructor instructor = getCurrentInstructor(email);

        return messageRepository.findChatHistory(instructor.getId(), studentId);
    }

    @Override
    public Message sendMessage(String email, Long receiverId, String content) {

        Instructor instructor = getCurrentInstructor(email);

        Message msg = new Message();
        msg.setSenderId(instructor.getId());
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setSenderRole("INSTRUCTOR");
        msg.setCreatedAt(LocalDateTime.now());

        return messageRepository.save(msg);
    }

    /**
     * Lấy Instructor hiện tại từ JWT
     */
    private Instructor getCurrentInstructor(String email) {

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return instructorRepository.findByUser_Id(student.getId())
                .orElseThrow(() -> new RuntimeException("Instructor profile not found"));
    }
}