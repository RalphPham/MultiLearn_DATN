package org.example.multileanproject.service;

import org.example.multileanproject.dto.ContactDTO;
import org.example.multileanproject.entity.Message;
import java.util.List;

public interface MessageService {
    List<ContactDTO> getContactsForInstructor(String email);
    List<Message> getChatHistory(String email, Long studentId);
    Message sendMessage(String email, Long receiverId, String content);
}