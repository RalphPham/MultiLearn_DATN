package org.example.multileanproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.multileanproject.dto.CategoryDTO;
import org.example.multileanproject.entity.Category;
import org.example.multileanproject.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category", description = "API quản lý danh mục khóa học")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // ========================================================================
    // 🔥 API TRẢ VỀ CÂY DANH MỤC (DÙNG CHO MENU ĐA CẤP VUE.JS)
    // ========================================================================
    @GetMapping
    @Operation(summary = "Lấy cây danh mục", description = "Dùng cho dropdown menu đa cấp như Udemy")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> rootEntities = categoryService.getAllCategoriesWithChildren();

        List<CategoryDTO> dtos = rootEntities.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // Hàm đệ quy chuyển đổi an toàn từ Category (Entity) sang CategoryDTO
    private CategoryDTO mapToDTO(Category entity) {
        if (entity == null) return null;

        List<CategoryDTO> childrenDTOs = null;
        if (entity.getChildren() != null && !entity.getChildren().isEmpty()) {
            childrenDTOs = entity.getChildren().stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        // ĐÃ SỬA LỖI Ở ĐÂY: Dùng từ khóa "new" thay vì ".builder()"
        return new CategoryDTO(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getIconUrl(),
                childrenDTOs
        );
    }
    // ========================================================================


    @GetMapping("/{id}")
    @Operation(summary = "Lấy danh mục theo ID")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Lấy danh mục theo slug")
    public ResponseEntity<Category> getCategoryBySlug(@PathVariable String slug) {
        Category category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/{parentId}/children")
    @Operation(summary = "Lấy danh sách danh mục con",
            description = "Lấy tất cả subcategory của một category")
    public ResponseEntity<List<Category>> getSubCategories(@PathVariable Long parentId) {
        List<Category> children = categoryService.getSubCategories(parentId);
        return ResponseEntity.ok(children);
    }

    @PostMapping
    @Operation(summary = "Tạo danh mục mới (ADMIN)",
            description = "Tạo một danh mục mới. Yêu cầu quyền ADMIN")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category createdCategory = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật danh mục (ADMIN)",
            description = "Cập nhật thông tin danh mục. Yêu cầu quyền ADMIN")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestBody Category category) {

        Category updatedCategory = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa danh mục (ADMIN)",
            description = "Xóa danh mục. Không thể xóa nếu có subcategories hoặc courses. Yêu cầu quyền ADMIN")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}