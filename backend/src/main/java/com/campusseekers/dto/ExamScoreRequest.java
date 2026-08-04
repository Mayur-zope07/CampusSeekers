package com.campusseekers.dto;

import com.campusseekers.entity.ExamName;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamScoreRequest {

    @NotNull(message = "Exam name is required")
    private ExamName examName;

    @NotNull(message = "Exam year is required")
    @Positive(message = "Exam year must be positive")
    private Integer examYear;

    @NotNull(message = "Rank is required")
    @Positive(message = "Rank must be positive")
    private Integer rank;

    @NotNull(message = "Percentile is required")
    @DecimalMin(value = "0.00", message = "Percentile cannot be less than 0.00")
    @DecimalMax(value = "100.00", message = "Percentile cannot exceed 100.00")
    private BigDecimal percentile;

    @Positive(message = "Marks must be positive")
    private Integer marks;
}
