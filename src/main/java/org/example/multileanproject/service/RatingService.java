package org.example.multileanproject.service;

import org.example.multileanproject.dto.RatingEligibilityResponseDTO;
import org.example.multileanproject.dto.RatingRequestDTO;
import org.example.multileanproject.dto.RatingResponseDTO;

import java.util.List;

public interface RatingService {

    RatingResponseDTO createRating(Long courseId, RatingRequestDTO request);
    RatingResponseDTO updateRating(Long courseId, RatingRequestDTO request);
    List<RatingResponseDTO> getRatingsByCourse(Long courseId);

    Double getAverageRating(Long courseId);

    Long countRatings(Long courseId);

    boolean hasStudentRatedCourse(Long courseId, Long studentId);

    List<RatingResponseDTO> getRatingsForInstructor(Long instructorId);

    List<RatingResponseDTO> getRatingsForCurrentInstructor();

    RatingEligibilityResponseDTO getMyRatingEligibility(Long courseId);

    void deleteRating(Long courseId);
}