package com.campusseekers.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "placements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Placement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @NotNull(message = "Year is required")
    @Column(name = "year", nullable = false)
    private Integer year;

    @NotNull(message = "Average package is required")
    @PositiveOrZero(message = "Average package must be positive or zero")
    @Column(name = "average_package", nullable = false, precision = 10, scale = 2)
    private BigDecimal averagePackage;

    @NotNull(message = "Highest package is required")
    @PositiveOrZero(message = "Highest package must be positive or zero")
    @Column(name = "highest_package", nullable = false, precision = 10, scale = 2)
    private BigDecimal highestPackage;

    @NotNull(message = "Placement ratio is required")
    @PositiveOrZero(message = "Placement ratio must be positive or zero")
    @Column(name = "placement_ratio", nullable = false, precision = 5, scale = 2)
    private BigDecimal placementRatio;

    @Column(name = "top_recruiters", columnDefinition = "TEXT")
    private String topRecruiters;
}
