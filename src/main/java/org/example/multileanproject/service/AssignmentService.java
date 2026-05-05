package org.example.multileanproject.service;

import org.example.multileanproject.entity.Assignment;
import java.util.List;

public interface AssignmentService {
    Assignment createAssignment(Assignment assignment);
    List<Assignment> getAssignmentsBySection(Long sectionId);
    void deleteAssignment(Long id); // Thêm xóa cho đầy đủ
}