package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    // Lấy danh sách section theo course (để hiển thị chi tiết)
    List<Section> findByCourse_IdOrderByOrderIndexAsc(Long courseId);

    // Lấy section kèm bài học (Fetch Join để tối ưu query)
    @Query("SELECT s FROM Section s LEFT JOIN FETCH s.lessons WHERE s.id = :id")
    Optional<Section> findByIdWithLessons(@Param("id") Long id);

    // Đếm số lượng chương
    Long countByCourse_Id(Long courseId);

    // Tìm thứ tự lớn nhất (dùng khi thêm chương lẻ)
    @Query("SELECT COALESCE(MAX(s.orderIndex), 0) FROM Section s WHERE s.course.id = :courseId")
    Integer findMaxOrderIndexByCourseId(@Param("courseId") Long courseId);

    // 🔥 [QUAN TRỌNG] Hàm xóa sạch chương của một khóa học
    // Dùng để reset nội dung khi giảng viên nhấn "Lưu thay đổi" từ Frontend
    @Modifying
    @Transactional
    @Query("DELETE FROM Section s WHERE s.course.id = :courseId")
    void deleteAllByCourseId(@Param("courseId") Long courseId);
}