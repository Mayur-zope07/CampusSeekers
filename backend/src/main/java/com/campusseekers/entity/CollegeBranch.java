package com.campusseekers.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "college_branches")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeBranch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotNull(message = "Intake capacity is required")
    @Positive(message = "Intake capacity must be positive")
    @Column(name = "intake_capacity", nullable = false)
    private Integer intakeCapacity;

    @NotNull(message = "Fees per year is required")
    @PositiveOrZero(message = "Fees must be positive or zero")
    @Column(name = "fees_per_year", nullable = false, precision = 12, scale = 2)
    private BigDecimal feesPerYear;

    @NotNull(message = "Duration in years is required")
    @Positive(message = "Duration must be positive")
    @Column(name = "duration_years", nullable = false)
    @Builder.Default
    private Integer durationYears = 4;

    @Builder.Default
    @OneToMany(mappedBy = "collegeBranch", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Cutoff> cutoffs = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "collegeBranch", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Shortlist> shortlists = new ArrayList<>();
}
