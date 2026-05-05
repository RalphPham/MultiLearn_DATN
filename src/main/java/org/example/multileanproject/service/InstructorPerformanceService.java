package org.example.multileanproject.service;

import org.example.multileanproject.dto.InstructorRevenueSummaryDTO;
import org.example.multileanproject.dto.InstructorStudentItemDTO;
import org.example.multileanproject.dto.InstructorStudentOverviewDTO;
import org.example.multileanproject.dto.LessonDropoffDTO;
import org.example.multileanproject.dto.RatingTrendDTO;

import java.time.LocalDate;
import java.util.List;

public interface InstructorPerformanceService {

    InstructorStudentOverviewDTO getStudentOverview(String instructorEmail);

    List<InstructorStudentItemDTO> getStudentItems(String instructorEmail);

    InstructorRevenueSummaryDTO getRevenueSummary(String instructorEmail, Integer year,
                                                  LocalDate fromDate, LocalDate toDate);

    List<LessonDropoffDTO> getLessonDropoff(String instructorEmail, Long courseId);

    List<InstructorStudentItemDTO> getAtRiskStudents(String instructorEmail);

    RatingTrendDTO getRatingTrend(String instructorEmail);
}