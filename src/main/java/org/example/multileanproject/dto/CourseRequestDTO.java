package org.example.multileanproject.dto;

import java.math.BigDecimal;
import java.util.List;

public class CourseRequestDTO {

    private String title;
    private String shortDescription;
    private String description;
    private String thumbnail;

    // ĐÃ XÓA QUYỀN ĐỊNH GIÁ TỪ GIẢNG VIÊN
    // private BigDecimal price;
    // private BigDecimal salePrice;

    private String level;
    private String language;
    private String learningOutcomes;
    private String status;
    private Long categoryId;
    private List<SectionRequestDTO> sections;

    public CourseRequestDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    // ĐÃ XÓA GETTER/SETTER CHO PRICE VÀ SALE_PRICE

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLearningOutcomes() {
        return learningOutcomes;
    }

    public void setLearningOutcomes(String learningOutcomes) {
        this.learningOutcomes = learningOutcomes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<SectionRequestDTO> getSections() {
        return sections;
    }

    public void setSections(List<SectionRequestDTO> sections) {
        this.sections = sections;
    }
}