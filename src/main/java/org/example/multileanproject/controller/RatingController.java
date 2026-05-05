package org.example.multileanproject.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.RatingEligibilityResponseDTO;
import org.example.multileanproject.dto.RatingRequestDTO;
import org.example.multileanproject.dto.RatingResponseDTO;
import org.example.multileanproject.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/course/{courseId}")
    public ResponseEntity<RatingResponseDTO> createRating(
            @PathVariable Long courseId,
            @Valid @RequestBody RatingRequestDTO request
    ) {
        return ResponseEntity.ok(ratingService.createRating(courseId, request));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<RatingResponseDTO>> getRatingsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(ratingService.getRatingsByCourse(courseId));
    }

    @GetMapping("/course/{courseId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long courseId) {
        return ResponseEntity.ok(ratingService.getAverageRating(courseId));
    }

    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Long> countRatings(@PathVariable Long courseId) {
        return ResponseEntity.ok(ratingService.countRatings(courseId));
    }

    @GetMapping("/course/{courseId}/student/{studentId}/check")
    public ResponseEntity<Boolean> hasStudentRatedCourse(
            @PathVariable Long courseId,
            @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(ratingService.hasStudentRatedCourse(courseId, studentId));
    }

    @GetMapping("/course/{courseId}/eligibility")
    public ResponseEntity<RatingEligibilityResponseDTO> getMyEligibility(@PathVariable Long courseId) {
        return ResponseEntity.ok(ratingService.getMyRatingEligibility(courseId));
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<RatingResponseDTO>> getRatingsForInstructor(@PathVariable Long instructorId) {
        return ResponseEntity.ok(ratingService.getRatingsForInstructor(instructorId));
    }

    @GetMapping("/instructor/me")
    public ResponseEntity<List<RatingResponseDTO>> getRatingsForCurrentInstructor() {
        return ResponseEntity.ok(ratingService.getRatingsForCurrentInstructor());
    }
    @DeleteMapping("/course/{courseId}")
    public ResponseEntity<Void> deleteRating(@PathVariable Long courseId) {
        ratingService.deleteRating(courseId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/course/{courseId}")
    public ResponseEntity<RatingResponseDTO> updateRating(
            @PathVariable Long courseId,
            @Valid @RequestBody RatingRequestDTO request
    ) {
        return ResponseEntity.ok(ratingService.updateRating(courseId, request));
    }
}