package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.entity.Assignment;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.repository.AssignmentRepository;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.service.AssignmentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;

    private void assertAssignmentOwnership(Assignment assignment) {
        if (assignment.getCourseId() == null) return;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equals(a.getAuthority()));
        if (isAdmin) return;
        Course course = courseRepository.findById(assignment.getCourseId()).orElse(null);
        if (course == null) return;
        if (course.getInstructor() == null || course.getInstructor().getUser() == null) return;
        String ownerEmail = course.getInstructor().getUser().getEmail();
        if (!auth.getName().equalsIgnoreCase(ownerEmail)) {
            throw new RuntimeException("Bạn không có quyền thao tác với bài tập này.");
        }
    }

    @Override
    public Assignment createAssignment(Assignment assignment) {
        assertAssignmentOwnership(assignment);
        return assignmentRepository.save(assignment);
    }

    @Override
    public List<Assignment> getAssignmentsBySection(Long sectionId) {
        return assignmentRepository.findBySectionId(sectionId);
    }

    @Override
    public void deleteAssignment(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        assertAssignmentOwnership(assignment);
        assignmentRepository.deleteById(id);
    }
}