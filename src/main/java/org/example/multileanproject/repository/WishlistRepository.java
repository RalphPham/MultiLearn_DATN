package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // ✅ Thêm hàm này để sửa lỗi "cannot find symbol"
    List<Wishlist> findByStudentId(Long studentId);

    boolean existsByStudent_IdAndCourse_Id(Long studentId, Long courseId);
    void deleteByStudent_IdAndCourse_Id(Long studentId, Long courseId);
}