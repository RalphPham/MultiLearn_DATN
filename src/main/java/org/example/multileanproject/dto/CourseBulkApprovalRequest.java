package org.example.multileanproject.dto;

import java.util.List;

public class CourseBulkApprovalRequest {
    private List<Long> courseIds;
    private String action; // APPROVE | REJECT
    private String reason;

    public List<Long> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<Long> courseIds) {
        this.courseIds = courseIds;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
