package com.campusseekers.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "recommendation_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private Recommendation recommendation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_branch_id", nullable = false)
    private CollegeBranch collegeBranch;

    @NotNull
    @Column(name = "closing_percentile", nullable = false, precision = 5, scale = 2)
    private BigDecimal closingPercentile;

    @NotNull
    @Column(name = "student_percentile", nullable = false, precision = 5, scale = 2)
    private BigDecimal studentPercentile;

    @NotNull
    @Column(name = "percentile_difference", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentileDifference;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_category", nullable = false, length = 50)
    private RecommendationCategory recommendationCategory;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_reason_code", nullable = false, length = 100)
    private RecommendationReasonCode recommendationReasonCode;

    @NotNull
    @Column(name = "human_readable_reason", nullable = false, length = 255)
    private String humanReadableReason;
}
