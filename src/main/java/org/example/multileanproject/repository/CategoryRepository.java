package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIsNull();
    List<Category> findByParent_Id(Long parentId);
    Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug);

    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL ORDER BY c.id ASC")
    List<Category> findRootCategoriesWithChildren();

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.id = :id")
    Optional<Category> findByIdWithChildren(@Param("id") Long id);

    // Đếm số khóa học (Bao gồm cả khóa học trong danh mục con)
    @Query("SELECT COUNT(c) FROM Course c WHERE c.category.id = :categoryId OR c.category.parent.id = :categoryId")
    Long countCoursesInCategoryRecursive(@Param("categoryId") Long categoryId);

    // 🔥 [MỚI] Đếm tổng số học viên (Cộng dồn field studentCount của các khóa học)
    @Query("SELECT COALESCE(SUM(c.studentCount), 0) FROM Course c WHERE c.category.id = :categoryId OR c.category.parent.id = :categoryId")
    Long countStudentsInCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT DISTINCT c FROM Category c INNER JOIN c.courses course WHERE course.status = 'PUBLISHED'")
    List<Category> findCategoriesWithPublishedCourses();
}