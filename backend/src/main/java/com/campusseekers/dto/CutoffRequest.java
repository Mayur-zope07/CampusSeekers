package com.campusseekers.dto;

import com.campusseekers.entity.Category;
import com.campusseekers.entity.ExamName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CutoffRequest {

    @NotNull(message = "College-Branch ID is required")
    private UUID collegeBranchId;

    @NotNull(message = "Exam name is required")
    private ExamName examName;

    @NotNull(message = "Year is required")
    @Positive(message = "Year must be positive")
    private Integer year;

    @NotNull(message = "Round is required")
    @Positive(message = "Round must be positive")
    private Integer round;

    @NotNull(message = "Category is required")
    private Category category;

    @NotBlank(message = "Seat type is required")
    private String rawSeatType;

    @NotBlank(message = "Stage is required")
    private String stage;

    @NotNull(message = "Closing rank is required")
    @Positive(message = "Closing rank must be positive")
    private Integer closingRank;

    @NotNull(message = "Closing percentile is required")
    @DecimalMin(value = "0.00", message = "Percentile cannot be less than 0.00")
    @DecimalMax(value = "100.00", message = "Percentile cannot exceed 100.00")
    private BigDecimal closingPercentile;
}
