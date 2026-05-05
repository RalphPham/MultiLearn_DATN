package org.example.multileanproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBasicInfoChangeRequestDTO {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    private String title;

    private Long categoryId;

    @Size(max = 500, message = "Mô tả ngắn tối đa 500 ký tự")
    private String shortDescription;

    private String description;

    private String learningOutcomes;

    @Size(max = 50, message = "Ngôn ngữ tối đa 50 ký tự")
    private String language;

    @Size(max = 20, message = "Trình độ tối đa 20 ký tự")
    private String level;

    private String thumbnail;

    @Size(max = 500, message = "Lý do tối đa 500 ký tự")
    private String requestNote;
}
