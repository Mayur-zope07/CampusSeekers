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

    @NotBlank(message = "Exam name is required")
    @Size(max = 50, message = "Exam name cannot exceed 50 characters")
    @Column(name = "exam_name", nullable = false, length = 50)
    private String examName;

    @NotNull(message = "Year is required")
    @Column(name = "year", nullable = false)
    private Integer year;

    @NotNull(message = "Round is required")
    @Column(name = "round", nullable = false)
    private Integer round;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @NotBlank(message = "Seat type is required")
    @Size(max = 50, message = "Seat type cannot exceed 50 characters")
    @Column(name = "seat_type", nullable = false, length = 50)
    private String seatType;

    @NotNull(message = "Closing rank is required")
    @PositiveOrZero(message = "Closing rank must be positive or zero")
    @Column(name = "closing_rank", nullable = false)
    private Integer closingRank;

    @NotNull(message = "Closing percentile is required")
    @PositiveOrZero(message = "Closing percentile must be positive or zero")
    @Column(name = "closing_percentile", nullable = false, precision = 5, scale = 2)
    private BigDecimal closingPercentile;
}
