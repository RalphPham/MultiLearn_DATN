package org.example.multileanproject.service;


import org.example.multileanproject.entity.Category;

import java.util.List;

/**
 * Service Interface cho Category
 * Định nghĩa các business logic liên quan đến danh mục
 */
public interface CategoryService {
    List<Category> getAllRootCategories();
    List<Category> getSubCategories(Long parentId);
    Category getCategoryById(Long id);
    Category getCategoryBySlug(String slug);
    Category createCategory(Category category);
    Category updateCategory(Long id, Category category);
    void deleteCategory(Long id);
    List<Category> getAllCategoriesWithChildren();

    // Thêm hàm này để khớp với Controller mới
    List<Category> getAllCategories();
}
