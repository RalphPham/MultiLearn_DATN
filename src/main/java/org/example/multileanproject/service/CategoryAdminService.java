package org.example.multileanproject.service;

import org.example.multileanproject.dto.CategoryDTO;
import org.example.multileanproject.entity.Category;
import org.example.multileanproject.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.text.Normalizer;
import java.util.regex.Pattern;

@Service
public class CategoryAdminService {
    private final CategoryRepository categoryRepository;

    public CategoryAdminService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // 1. Lấy danh sách (Kèm thống kê)
    public List<CategoryDTO> getAllCategories() {
        // Lấy tất cả danh mục (bao gồm cả con) để hiển thị list phẳng trên bảng
        return categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 2. Thêm mới
    @Transactional
    public void createCategory(CategoryDTO dto) {
        Category cat = new Category();
        cat.setName(dto.getName());

        // Tự tạo slug nếu không có
        String slug = (dto.getSlug() == null || dto.getSlug().isEmpty())
                ? generateSlug(dto.getName()) : dto.getSlug();
        // Đảm bảo slug không trùng
        if (categoryRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }
        cat.setSlug(slug);

        cat.setDescription(dto.getDescription());
        cat.setIconUrl(dto.getIconUrl());

        // Xử lý cha-con
        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId()).orElse(null);
            cat.setParent(parent);
        }

        categoryRepository.save(cat);
    }

    // 3. Cập nhật
    @Transactional
    public void updateCategory(Long id, CategoryDTO dto) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        cat.setName(dto.getName());
        cat.setDescription(dto.getDescription());
        cat.setIconUrl(dto.getIconUrl());

        // Chỉ update slug nếu người dùng nhập mới (để tránh hỏng SEO cũ)
        if (dto.getSlug() != null && !dto.getSlug().isEmpty()) {
            // Kiểm tra slug mới không trùng với category khác
            categoryRepository.findBySlug(dto.getSlug()).ifPresent(existing -> {
                if (!existing.getId().equals(cat.getId())) {
                    throw new RuntimeException("Slug \"" + dto.getSlug() + "\" đã được sử dụng bởi danh mục khác.");
                }
            });
            cat.setSlug(dto.getSlug());
        }

        // Xử lý cha-con
        if (dto.getParentId() != null) {
            // Không cho phép chọn chính mình làm cha
            if (dto.getParentId().equals(cat.getId())) {
                throw new RuntimeException("Không thể chọn chính mình làm danh mục cha!");
            }
            Category parent = categoryRepository.findById(dto.getParentId()).orElse(null);
            cat.setParent(parent);
        } else {
            cat.setParent(null); // Set về null để thành danh mục gốc
        }

        categoryRepository.save(cat);
    }

    // 4. Xóa
    @Transactional
    public void deleteCategory(Long id) {
        // Kiểm tra ràng buộc: Nếu có khóa học thì không cho xóa
        long count = categoryRepository.countCoursesInCategoryRecursive(id);
        if (count > 0) {
            throw new RuntimeException("Không thể xóa danh mục đang chứa " + count + " khóa học!");
        }
        categoryRepository.deleteById(id);
    }

    // Helper: Convert Entity -> DTO
    private CategoryDTO convertToDTO(Category cat) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(cat.getId());
        dto.setName(cat.getName());
        dto.setSlug(cat.getSlug());
        dto.setDescription(cat.getDescription());
        dto.setIconUrl(cat.getIconUrl());

        if (cat.getParent() != null) {
            dto.setParentId(cat.getParent().getId());
            dto.setParentName(cat.getParent().getName());
        }

        // Gọi Repository để lấy số liệu thống kê
        dto.setCourseCount(categoryRepository.countCoursesInCategoryRecursive(cat.getId()));
        dto.setStudentCount(categoryRepository.countStudentsInCategory(cat.getId()));

        return dto;
    }

    // Helper: Tạo Slug chuẩn SEO (Tiếng Việt -> Không dấu)
    private String generateSlug(String name) {
        String temp = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("")
                .toLowerCase()
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");
    }
}