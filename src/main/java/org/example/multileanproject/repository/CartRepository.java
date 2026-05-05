package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Thêm dấu gạch dưới "_" để JPA hiểu rõ là tìm theo ID của Student
    @Query("SELECT c FROM Cart c WHERE c.student.id = :studentId")
    Optional<Cart> findByStudent_Id(Long studentId);
    Cart findByStudentId(Long studentId);
}