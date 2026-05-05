package org.example.multileanproject.service;

import org.example.multileanproject.dto.CourseChangeRequestDTO;
import org.example.multileanproject.dto.CreateBasicInfoChangeRequestDTO;
import org.example.multileanproject.dto.ReviewCourseChangeRequestDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface CourseChangeRequestService {

    CourseChangeRequestDTO createBasicInfoRequest(
            Long courseId, CreateBasicInfoChangeRequestDTO dto, String instructorEmail);

    void cancelRequest(Long requestId, String instructorEmail);

    Optional<CourseChangeRequestDTO> getLatestRequest(Long courseId, String instructorEmail);

    Page<CourseChangeRequestDTO> listPendingRequests(int page, int size);

    CourseChangeRequestDTO approveRequest(
            Long requestId, ReviewCourseChangeRequestDTO dto, String adminUsername);

    CourseChangeRequestDTO rejectRequest(
            Long requestId, ReviewCourseChangeRequestDTO dto, String adminUsername);
}
