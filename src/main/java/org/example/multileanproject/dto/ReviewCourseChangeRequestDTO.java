package org.example.multileanproject.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCourseChangeRequestDTO {

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String adminNote;
}
