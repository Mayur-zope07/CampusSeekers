package com.campusseekers.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "exam_scores")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamScore extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_profile_id", nullable = false)
    private StudentProfile studentProfile;

    @NotBlank(message = "Exam name is required")
    @Size(max = 50, message = "Exam name cannot exceed 50 characters")
    @Column(name = "exam_name", nullable = false, length = 50)
    private String examName;

    @NotNull(message = "Score percentile is required")
    @PositiveOrZero(message = "Score percentile must be positive or zero")
    @Column(name = "score_percentile", nullable = false, precision = 5, scale = 2)
    private BigDecimal scorePercentile;

    @NotNull(message = "Score rank is required")
    @PositiveOrZero(message = "Score rank must be positive or zero")
    @Column(name = "score_rank", nullable = false)
    private Integer scoreRank;

    @NotNull(message = "Exam year is required")
    @Column(name = "exam_year", nullable = false)
    private Integer examYear;
}
