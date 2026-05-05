package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findBySectionId(Long sectionId);
}