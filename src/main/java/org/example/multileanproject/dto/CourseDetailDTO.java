package org.example.multileanproject.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CourseDetailDTO {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private String shortDescription;
    private String thumbnail;
    private BigDecimal price;
    private BigDecimal salePrice;

    private String instructorName;
    private String instructorAvatar;

    private String authorName;
    private String authorAvatar;

    private String level;
    private String language;
    private String learningOutcomes;
    private String status;

    private Long instructorUserId;
    private Long categoryId;
    private String categoryName;

    private Double averageRating;
    private Integer studentCount;
    private Integer totalLessons;
    private Integer totalDuration;

    private Boolean isOwned;
    private Boolean isInstructorOwner;
    private Boolean isCourseCompleted;
    private Boolean certificateIssued;

    // 🔥 BỔ SUNG 3 BIẾN NÀY CHO TÍNH NĂNG FLASH SALE 🔥
    private Integer totalSlots;
    private Integer soldSlots;
    private Boolean isFlashSale;

    // rental
    private Boolean rentalEnabled;
    private BigDecimal rentalPrice7d;
    private BigDecimal rentalPrice30d;
    private BigDecimal rentalPrice90d;
    private LocalDateTime rentalExpiresAt;

    private LocalDateTime updatedAt;
    private List<SectionDTO> sections;

    public CourseDetailDTO() {
    }

    // --- GETTERS & SETTERS FLASH SALE ---
    public Integer getTotalSlots() { return totalSlots; }
    public void setTotalSlots(Integer totalSlots) { this.totalSlots = totalSlots; }
    public Integer getSoldSlots() { return soldSlots; }
    public void setSoldSlots(Integer soldSlots) { this.soldSlots = soldSlots; }
    public Boolean getIsFlashSale() { return isFlashSale; }
    public void setIsFlashSale(Boolean flashSale) { isFlashSale = flashSale; }
    // --- GETTERS & SETTERS RENTAL ---
    public Boolean getRentalEnabled() { return rentalEnabled; }
    public void setRentalEnabled(Boolean rentalEnabled) { this.rentalEnabled = rentalEnabled; }
    public BigDecimal getRentalPrice7d() { return rentalPrice7d; }
    public void setRentalPrice7d(BigDecimal rentalPrice7d) { this.rentalPrice7d = rentalPrice7d; }
    public BigDecimal getRentalPrice30d() { return rentalPrice30d; }
    public void setRentalPrice30d(BigDecimal rentalPrice30d) { this.rentalPrice30d = rentalPrice30d; }
    public BigDecimal getRentalPrice90d() { return rentalPrice90d; }
    public void setRentalPrice90d(BigDecimal rentalPrice90d) { this.rentalPrice90d = rentalPrice90d; }
    public LocalDateTime getRentalExpiresAt() { return rentalExpiresAt; }
    public void setRentalExpiresAt(LocalDateTime rentalExpiresAt) { this.rentalExpiresAt = rentalExpiresAt; }
    // ---------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public String getInstructorAvatar() {
        return instructorAvatar;
    }

    public void setInstructorAvatar(String instructorAvatar) {
        this.instructorAvatar = instructorAvatar;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

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

    public Long getInstructorUserId() {
        return instructorUserId;
    }

    public void setInstructorUserId(Long instructorUserId) {
        this.instructorUserId = instructorUserId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public Integer getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(Integer totalLessons) {
        this.totalLessons = totalLessons;
    }

    public Integer getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(Integer totalDuration) {
        this.totalDuration = totalDuration;
    }

    public Boolean getIsOwned() {
        return isOwned;
    }

    public void setIsOwned(Boolean isOwned) {
        this.isOwned = isOwned;
    }

    public Boolean getIsInstructorOwner() {
        return isInstructorOwner;
    }

    public void setIsInstructorOwner(Boolean isInstructorOwner) {
        this.isInstructorOwner = isInstructorOwner;
    }

    public Boolean getIsCourseCompleted() {
        return isCourseCompleted;
    }

    public void setIsCourseCompleted(Boolean isCourseCompleted) {
        this.isCourseCompleted = isCourseCompleted;
    }

    public Boolean getCertificateIssued() {
        return certificateIssued;
    }

    public void setCertificateIssued(Boolean certificateIssued) {
        this.certificateIssued = certificateIssued;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<SectionDTO> getSections() {
        return sections;
    }

    public void setSections(List<SectionDTO> sections) {
        this.sections = sections;
    }

    public static class SectionDTO {
        private Long id;
        private String title;
        private Integer lessonCount;
        private Integer totalDuration;
        private List<LessonDTO> lessons;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getLessonCount() {
            return lessonCount;
        }

        public void setLessonCount(Integer lessonCount) {
            this.lessonCount = lessonCount;
        }

        public Integer getTotalDuration() {
            return totalDuration;
        }

        public void setTotalDuration(Integer totalDuration) {
            this.totalDuration = totalDuration;
        }

        public List<LessonDTO> getLessons() {
            return lessons;
        }

        public void setLessons(List<LessonDTO> lessons) {
            this.lessons = lessons;
        }
    }

    public static class LessonDTO {
        private Long id;
        private String title;
        private String type;
        private Integer duration;
        private Boolean isPreview;
        private Boolean isCompleted;
        private String videoUrl;
        private String contentText;
        private String documentUrl;
        private Long quizId;
        private Integer quizPassingScore;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getDuration() {
            return duration;
        }

        public void setDuration(Integer duration) {
            this.duration = duration;
        }

        public Boolean getIsPreview() {
            return isPreview;
        }

        public void setIsPreview(Boolean isPreview) {
            this.isPreview = isPreview;
        }

        public Boolean getIsCompleted() {
            return isCompleted;
        }

        public void setIsCompleted(Boolean isCompleted) {
            this.isCompleted = isCompleted;
        }

        public String getVideoUrl() {
            return videoUrl;
        }

        public void setVideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
        }

        public String getContentText() {
            return contentText;
        }

        public void setContentText(String contentText) {
            this.contentText = contentText;
        }

        public String getDocumentUrl() {
            return documentUrl;
        }

        public void setDocumentUrl(String documentUrl) {
            this.documentUrl = documentUrl;
        }

        public Long getQuizId() {
            return quizId;
        }

        public void setQuizId(Long quizId) {
            this.quizId = quizId;
        }

        public Integer getQuizPassingScore() {
            return quizPassingScore;
        }

        public void setQuizPassingScore(Integer quizPassingScore) {
            this.quizPassingScore = quizPassingScore;
        }
    }
}