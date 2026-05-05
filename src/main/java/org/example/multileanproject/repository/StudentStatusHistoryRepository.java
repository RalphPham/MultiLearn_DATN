package org.example.multileanproject.repository;

import org.example.multileanproject.entity.StudentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentStatusHistoryRepository extends JpaRepository<StudentStatusHistory, Long> {
    List<StudentStatusHistory> findByStudent_IdOrderByCreatedAtDesc(Long studentId);
    Optional<StudentStatusHistory> findTopByStudentIdOrderByIdDesc(Long studentId);
    List<StudentStatusHistory> findByStudentIdOrderByIdDesc(Long studentId);
}
