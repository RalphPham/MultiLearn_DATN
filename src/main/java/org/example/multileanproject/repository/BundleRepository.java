package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Bundle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BundleRepository extends JpaRepository<Bundle, Long> {
    // Tìm gói theo Email giảng viên (để hiện lên Dashboard)
    List<Bundle> findByInstructor_User_Email(String email);
}