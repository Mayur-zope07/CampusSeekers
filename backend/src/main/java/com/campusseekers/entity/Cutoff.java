package com.campusseekers.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "cutoffs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cutoff extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_branch_id", nullable = false)
    private CollegeBranch collegeBranch;

    @NotNull(message = "Exam name is required")
    @Column(name = "exam_name", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ExamName examName;

    @NotNull(message = "Year is required")
    @Column(name = "year", nullable = false)
    private Integer year;

    @NotNull(message = "Round is required")
    @Column(name = "round", nullable = false)
    private Integer round;

    @NotNull(message = "Category is required")
    @Column(name = "category", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Category category;

    @NotBlank(message = "Seat type is required")
    @Column(name = "seat_type", nullable = false, length = 50)
    private String rawSeatType;

    @NotBlank(message = "Stage is required")
    @Column(name = "stage", nullable = false, length = 50)
    private String stage;

    @NotNull(message = "Closing rank is required")
    @PositiveOrZero(message = "Closing rank must be positive or zero")
    @Column(name = "closing_rank", nullable = false)
    private Integer closingRank;

    @NotNull(message = "Closing percentile is required")
    @PositiveOrZero(message = "Closing percentile must be positive or zero")
    @Column(name = "closing_percentile", nullable = false, precision = 5, scale = 2)
    private BigDecimal closingPercentile;
}
