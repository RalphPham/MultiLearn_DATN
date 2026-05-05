package org.example.multileanproject.controller;

import org.example.multileanproject.dto.CategoryDTO;
import org.example.multileanproject.service.CategoryAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class CategoryAdminController {

    private final CategoryAdminService categoryAdminService;

    public CategoryAdminController(CategoryAdminService categoryAdminService) {
        this.categoryAdminService = categoryAdminService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','CATEGORY_VIEW')")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryAdminService.getAllCategories());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','CATEGORY_MANAGE')")
    public ResponseEntity<Void> createCategory(@RequestBody CategoryDTO dto) {
        categoryAdminService.createCategory(dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','CATEGORY_MANAGE')")
    public ResponseEntity<Void> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        categoryAdminService.updateCategory(id, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','CATEGORY_DELETE')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryAdminService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
}