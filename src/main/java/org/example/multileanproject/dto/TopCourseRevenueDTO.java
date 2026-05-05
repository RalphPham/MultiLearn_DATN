package org.example.multileanproject.dto;

import java.math.BigDecimal;

public class TopCourseRevenueDTO {

    private Long courseId;
    private String courseTitle;
    private String thumbnail;
    private Long students;
    private Double revenue;

    public TopCourseRevenueDTO() {
    }

    public TopCourseRevenueDTO(Long courseId, String courseTitle,
                               String thumbnail, Long students, Double revenue) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.thumbnail = thumbnail;
        this.students = students;
        this.revenue = revenue;
    }

    public TopCourseRevenueDTO(Long courseId, String courseTitle,
                               String thumbnail, Long students, BigDecimal revenue) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.thumbnail = thumbnail;
        this.students = students != null ? students : 0L;
        this.revenue = revenue != null ? revenue.doubleValue() : 0.0;
    }

    public TopCourseRevenueDTO(Long courseId, String courseTitle,
                               String thumbnail, Integer students, BigDecimal revenue) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.thumbnail = thumbnail;
        this.students = students != null ? students.longValue() : 0L;
        this.revenue = revenue != null ? revenue.doubleValue() : 0.0;
    }

    // Dự phòng khi COALESCE(SUM(null), 0) trả về Integer thay vì BigDecimal
    public TopCourseRevenueDTO(Long courseId, String courseTitle,
                               String thumbnail, Number students, Number revenue) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.thumbnail = thumbnail;
        this.students = students != null ? students.longValue() : 0L;
        this.revenue = revenue != null ? revenue.doubleValue() : 0.0;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Long getStudents() {
        return students;
    }

    public void setStudents(Long students) {
        this.students = students;
    }

    public Double getRevenue() {
        return revenue;
    }

    public void setRevenue(Double revenue) {
        this.revenue = revenue;
    }
}