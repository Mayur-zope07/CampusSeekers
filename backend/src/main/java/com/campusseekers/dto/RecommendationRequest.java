package com.campusseekers.dto;

import com.campusseekers.entity.Category;
import com.campusseekers.entity.CollegeType;
import com.campusseekers.entity.ExamName;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record RecommendationRequest(
        @NotNull(message = "Exam name is required")
        ExamName exam,

        @NotNull(message = "Year is required")
        @Min(value = 1900, message = "Year must be supported")
        Integer year,

        @NotNull(message = "Percentile is required")
        @DecimalMin(value = "0.0", message = "Percentile must be between 0 and 100")
        @DecimalMax(value = "100.0", message = "Percentile must be between 0 and 100")
        BigDecimal percentile,

        @Positive(message = "Rank must be positive")
        Integer rank,

        @NotNull(message = "Category is required")
        Category category,

        List<String> preferredBranches,
        List<String> preferredCities,
        List<CollegeType> preferredCollegeTypes,
        String minimumNAAC,

        @Positive(message = "Maximum fees must be positive")
        BigDecimal maximumFees
) {}
