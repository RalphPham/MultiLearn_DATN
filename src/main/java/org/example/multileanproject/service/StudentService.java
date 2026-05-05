package org.example.multileanproject.service;

import org.example.multileanproject.dto.StudentStatusRequestDTO;
import org.example.multileanproject.entity.Student;
import java.util.Map;

public interface StudentService {
    Student getStudentById(Long id);
    Student getStudentByEmail(String email);
    Student updateStudent(Long id, Map<String, Object> updates);
    void upgradeToInstructor(String email);
    void updateStudentStatus(Long studentId, StudentStatusRequestDTO request, String adminEmail);
}