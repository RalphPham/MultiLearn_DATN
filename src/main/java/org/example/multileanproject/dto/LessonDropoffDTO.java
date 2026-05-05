package org.example.multileanproject.dto;

public class LessonDropoffDTO {

    private Long lessonId;
    private String lessonTitle;
    private String sectionTitle;
    private long completedCount;
    private long enrolledCount;
    private double completionRate;

    public LessonDropoffDTO() {}

    public LessonDropoffDTO(Long lessonId, String lessonTitle, String sectionTitle,
                            long completedCount, long enrolledCount) {
        this.lessonId = lessonId;
        this.lessonTitle = lessonTitle;
        this.sectionTitle = sectionTitle;
        this.completedCount = completedCount;
        this.enrolledCount = enrolledCount;
        this.completionRate = enrolledCount > 0
                ? Math.round((completedCount * 100.0 / enrolledCount) * 10.0) / 10.0
                : 0.0;
    }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public String getLessonTitle() { return lessonTitle; }
    public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }

    public String getSectionTitle() { return sectionTitle; }
    public void setSectionTitle(String sectionTitle) { this.sectionTitle = sectionTitle; }

    public long getCompletedCount() { return completedCount; }
    public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }

    public long getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(long enrolledCount) { this.enrolledCount = enrolledCount; }

    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
}
