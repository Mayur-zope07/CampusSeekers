package com.campusseekers.entity;

import com.campusseekers.util.CollegeTypeListConverter;
import com.campusseekers.util.StringListConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recommendations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_profile_id", nullable = false)
    private StudentProfile studentProfile;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "exam_name", nullable = false, length = 50)
    private ExamName examName;

    @NotNull
    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @NotNull
    @Column(name = "percentile", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentile;

    @Column(name = "rank")
    private Integer rank;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private Category category;

    @Convert(converter = StringListConverter.class)
    @Column(name = "preferred_branches", columnDefinition = "TEXT")
    private List<String> preferredBranches = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(name = "preferred_cities", columnDefinition = "TEXT")
    private List<String> preferredCities = new ArrayList<>();

    @Convert(converter = CollegeTypeListConverter.class)
    @Column(name = "preferred_college_types", columnDefinition = "TEXT")
    private List<CollegeType> preferredCollegeTypes = new ArrayList<>();

    @Column(name = "minimum_naac", length = 10)
    private String minimumNaac;

    @Column(name = "maximum_fees", precision = 12, scale = 2)
    private BigDecimal maximumFees;

    @NotNull
    @Column(name = "execution_time_ms", nullable = false)
    private Integer executionTimeMs;

    @NotNull
    @Column(name = "evaluated_count", nullable = false)
    private Integer evaluatedCount;

    @NotNull
    @Column(name = "filtered_count", nullable = false)
    private Integer filteredCount;

    @NotNull
    @Column(name = "returned_count", nullable = false)
    private Integer returnedCount;

    @NotNull
    @Column(name = "safe_count", nullable = false)
    private Integer safeCount;

    @NotNull
    @Column(name = "target_count", nullable = false)
    private Integer targetCount;

    @NotNull
    @Column(name = "dream_count", nullable = false)
    private Integer dreamCount;

    @NotNull
    @Column(name = "engine_version", nullable = false, length = 50)
    private String engineVersion;

    @NotNull
    @Column(name = "algorithm_version", nullable = false, length = 50)
    private String algorithmVersion;

    @NotNull
    @Column(name = "safe_threshold", nullable = false, precision = 5, scale = 2)
    private BigDecimal safeThreshold;

    @NotNull
    @Column(name = "target_threshold", nullable = false, precision = 5, scale = 2)
    private BigDecimal targetThreshold;

    @NotNull
    @Column(name = "dream_threshold", nullable = false, precision = 5, scale = 2)
    private BigDecimal dreamThreshold;

    @NotNull
    @Column(name = "cache_hit", nullable = false)
    @Builder.Default
    private Boolean cacheHit = false;

    @OneToMany(mappedBy = "recommendation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecommendationItem> items = new ArrayList<>();
}
