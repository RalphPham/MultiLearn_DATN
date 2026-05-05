package org.example.multileanproject.repository;

import org.example.multileanproject.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // --- SỬA LỖI QUAN TRỌNG TẠI ĐÂY ---
    // Vì CartItem không có studentId, ta phải đi qua: CartItem -> Cart -> Student -> Id
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.student.id = :studentId")
    List<CartItem> findByStudentId(@Param("studentId") Long studentId);

    // Hàm xóa (giữ nguyên hoặc dùng deleteAll của JpaRepository cũng được)
    // Ở đây ta dùng Query để xóa nhanh theo ID sinh viên nếu muốn
    @Query("DELETE FROM CartItem ci WHERE ci.cart.student.id = :studentId")
    void deleteAllByStudentId(@Param("studentId") Long studentId);
    boolean existsByCart_Student_IdAndCourse_Id(Long studentId, Long courseId);

    // Xóa sản phẩm khỏi giỏ
    void deleteByCart_Student_IdAndCourse_Id(Long studentId, Long courseId);
}