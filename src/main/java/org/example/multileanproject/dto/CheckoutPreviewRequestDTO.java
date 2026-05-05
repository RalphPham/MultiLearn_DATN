package org.example.multileanproject.dto;

public class CheckoutPreviewRequestDTO {

    private Long courseId;
    private String couponCode;
    private String note;

    public CheckoutPreviewRequestDTO() {
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}