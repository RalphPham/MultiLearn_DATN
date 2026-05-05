package org.example.multileanproject.service;

import org.example.multileanproject.entity.Category;
import org.example.multileanproject.exception.ResourceNotFoundException;
import org.example.multileanproject.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
// Tạm tắt import Redis
// import org.springframework.cache.annotation.CacheEvict;
// import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    // @Cacheable(value = "categories", key = "'root'") // Đã tắt Redis
    public List<Category> getAllRootCategories() {
        return categoryRepository.findByParentIsNull();
    }

    @Override
    @Transactional(readOnly = true)
    // @Cacheable(value = "categories", key = "'sub:' + #parentId") // Đã tắt Redis
    public List<Category> getSubCategories(Long parentId) {
        categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", parentId));

        return categoryRepository.findByParent_Id(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    // @Cacheable(value = "categories", key = "'id:' + #id") // Đã tắt Redis
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    // @Cacheable(value = "categories", key = "'slug:' + #slug") // Đã tắt Redis
    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
    }

    @Override
    // @CacheEvict(value = "categories", allEntries = true) // Đã tắt Redis
    public Category createCategory(Category category) {
        if (categoryRepository.existsBySlug(category.getSlug())) {
            throw new RuntimeException("Slug danh mục đã tồn tại: " + category.getSlug());
        }
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được rỗng");
        }
        if (category.getParent() != null && category.getParent().getId() != null) {
            Category parent = categoryRepository.findById(category.getParent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", "id", category.getParent().getId()));
            category.setParent(parent);
        }

        return categoryRepository.save(category);
    }

    @Override
    // @CacheEvict(value = "categories", allEntries = true) // Đã tắt Redis
    public Category updateCategory(Long id, Category category) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        if (!existingCategory.getSlug().equals(category.getSlug())
                && categoryRepository.existsBySlug(category.getSlug())) {
            throw new RuntimeException("Slug danh mục đã tồn tại: " + category.getSlug());
        }
        existingCategory.setName(category.getName());
        existingCategory.setSlug(category.getSlug());
        existingCategory.setDescription(category.getDescription());
        existingCategory.setIconUrl(category.getIconUrl());
        if (category.getParent() != null && category.getParent().getId() != null) {
            if (category.getParent().getId().equals(id)) {
                throw new IllegalArgumentException("Category không thể là parent của chính nó");
            }

            Category parent = categoryRepository.findById(category.getParent().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", "id", category.getParent().getId()));
            existingCategory.setParent(parent);
        } else {
            existingCategory.setParent(null);
        }

        return categoryRepository.save(existingCategory);
    }

    @Override
    // @CacheEvict(value = "categories", allEntries = true) // Đã tắt Redis
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        if (!category.getChildren().isEmpty()) {
            throw new RuntimeException(
                    "Không thể xóa danh mục này vì có " + category.getChildren().size() +
                            " danh mục con. Vui lòng xóa danh mục con trước."
            );
        }
        if (!category.getCourses().isEmpty()) {
            throw new RuntimeException(
                    "Không thể xóa danh mục này vì có " + category.getCourses().size() +
                            " khóa học. Vui lòng chuyển khóa học sang danh mục khác trước."
            );
        }

        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    // @Cacheable(value = "categories", key = "'allWithChildren'") // Đã tắt Redis
    public List<Category> getAllCategoriesWithChildren() {
        return categoryRepository.findRootCategoriesWithChildren();
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}