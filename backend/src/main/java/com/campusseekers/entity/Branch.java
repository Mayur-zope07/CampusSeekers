package com.campusseekers.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "branches")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Branch extends BaseEntity {

    @NotBlank(message = "Branch name is required")
    @Size(max = 255, message = "Branch name cannot exceed 255 characters")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotBlank(message = "Branch code is required")
    @Size(max = 50, message = "Branch code cannot exceed 50 characters")
    @Column(name = "branch_code", unique = true, nullable = false, length = 50)
    private String branchCode;

    @Builder.Default
    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CollegeBranch> collegeBranches = new ArrayList<>();
}
