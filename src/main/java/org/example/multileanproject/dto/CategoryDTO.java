package org.example.multileanproject.dto;

import org.mapstruct.Builder;

import java.io.Serializable;
import java.util.List; // Nhớ import List nhé

public class CategoryDTO implements Serializable {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String iconUrl;

    // Thống kê (Dành cho Admin)
    private Long courseCount;
    private Long studentCount;
    private Long parentId;
    private String parentName;

    // 🔥 [MỚI] Danh sách danh mục con để vẽ menu Dropdown nhiều cấp như Udemy
    private List<CategoryDTO> children;

    public CategoryDTO() {}

    // ✅ HÀM TẠO 1: Dành cho CategoryController cũ (4 tham số)
    // Giúp sửa lỗi: no suitable constructor found...
    public CategoryDTO(Long id, String name, String slug, String iconUrl) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.iconUrl = iconUrl;
    }

    // ✅ HÀM TẠO 2: Dành cho CategoryAdminService mới (Full tham số)
    public CategoryDTO(Long id, String name, String slug, String description, String iconUrl, Long courseCount, Long studentCount) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.iconUrl = iconUrl;
        this.courseCount = courseCount;
        this.studentCount = studentCount;
    }

    // ✅ HÀM TẠO 3: Dành riêng cho Menu Cây Đa Cấp (Gắn thêm children)
    public CategoryDTO(Long id, String name, String slug, String iconUrl, List<CategoryDTO> children) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.iconUrl = iconUrl;
        this.children = children;
    }

    // ==========================================
    // Getters & Setters
    // ==========================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public Long getCourseCount() { return courseCount; }
    public void setCourseCount(Long courseCount) { this.courseCount = courseCount; }

    public Long getStudentCount() { return studentCount; }
    public void setStudentCount(Long studentCount) { this.studentCount = studentCount; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    // 🔥 [MỚI] Getter & Setter cho danh mục con
    public List<CategoryDTO> getChildren() { return children; }
    public void setChildren(List<CategoryDTO> children) { this.children = children; }
}