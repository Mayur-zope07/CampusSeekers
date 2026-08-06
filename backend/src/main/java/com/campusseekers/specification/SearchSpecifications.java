package com.campusseekers.specification;

import com.campusseekers.entity.*;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class SearchSpecifications {

    // --- College Specifications ---

    public static Specification<College> collegeHasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("city")), pattern),
                    cb.like(cb.lower(root.get("state")), pattern),
                    cb.like(cb.lower(root.get("collegeCode")), pattern)
            );
        };
    }

    public static Specification<College> collegeHasName(String name) {
        return (root, query, cb) -> (name == null || name.isBlank()) ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<College> collegeHasCode(String code) {
        return (root, query, cb) -> (code == null || code.isBlank()) ? null : cb.equal(cb.lower(root.get("collegeCode")), code.toLowerCase().trim());
    }

    public static Specification<College> collegeHasCity(String city) {
        return (root, query, cb) -> (city == null || city.isBlank()) ? null : cb.equal(cb.lower(root.get("city")), city.toLowerCase().trim());
    }

    public static Specification<College> collegeHasState(String state) {
        return (root, query, cb) -> (state == null || state.isBlank()) ? null : cb.equal(cb.lower(root.get("state")), state.toLowerCase().trim());
    }

    public static Specification<College> collegeHasType(CollegeType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("collegeType"), type);
    }

    public static Specification<College> collegeHasNaacGrade(String naacGrade) {
        return (root, query, cb) -> (naacGrade == null || naacGrade.isBlank()) ? null : cb.equal(root.get("naacGrade"), naacGrade.trim());
    }

    public static Specification<College> collegeHasNba(Boolean nba) {
        return (root, query, cb) -> nba == null ? null : cb.equal(root.get("nbaAccredited"), nba);
    }

    public static Specification<College> collegeHasStatus(CollegeStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    // --- Branch Specifications ---

    public static Specification<Branch> branchHasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("branchCode")), pattern)
            );
        };
    }

    public static Specification<Branch> branchHasName(String name) {
        return (root, query, cb) -> (name == null || name.isBlank()) ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Branch> branchHasCode(String code) {
        return (root, query, cb) -> (code == null || code.isBlank()) ? null : cb.equal(cb.lower(root.get("branchCode")), code.toLowerCase().trim());
    }

    // --- CollegeBranch Specifications ---

    public static Specification<CollegeBranch> cbHasCollege(String collegeFilter) {
        return (root, query, cb) -> {
            if (collegeFilter == null || collegeFilter.isBlank()) return null;
            Join<CollegeBranch, College> collegeJoin = root.join("college");
            return cb.or(
                    cb.equal(cb.lower(collegeJoin.get("collegeCode")), collegeFilter.toLowerCase().trim()),
                    cb.like(cb.lower(collegeJoin.get("name")), "%" + collegeFilter.toLowerCase() + "%")
            );
        };
    }

    public static Specification<CollegeBranch> cbHasBranch(String branchFilter) {
        return (root, query, cb) -> {
            if (branchFilter == null || branchFilter.isBlank()) return null;
            Join<CollegeBranch, Branch> branchJoin = root.join("branch");
            return cb.or(
                    cb.equal(cb.lower(branchJoin.get("branchCode")), branchFilter.toLowerCase().trim()),
                    cb.like(cb.lower(branchJoin.get("name")), "%" + branchFilter.toLowerCase() + "%")
            );
        };
    }

    public static Specification<CollegeBranch> cbHasFeesRange(BigDecimal minFees, BigDecimal maxFees) {
        return (root, query, cb) -> {
            if (minFees == null && maxFees == null) return null;
            if (minFees != null && maxFees != null) {
                return cb.between(root.get("feesPerYear"), minFees, maxFees);
            } else if (minFees != null) {
                return cb.greaterThanOrEqualTo(root.get("feesPerYear"), minFees);
            } else {
                return cb.lessThanOrEqualTo(root.get("feesPerYear"), maxFees);
            }
        };
    }

    public static Specification<CollegeBranch> cbHasIntakeRange(Integer minIntake, Integer maxIntake) {
        return (root, query, cb) -> {
            if (minIntake == null && maxIntake == null) return null;
            if (minIntake != null && maxIntake != null) {
                return cb.between(root.get("intakeCapacity"), minIntake, maxIntake);
            } else if (minIntake != null) {
                return cb.greaterThanOrEqualTo(root.get("intakeCapacity"), minIntake);
            } else {
                return cb.lessThanOrEqualTo(root.get("intakeCapacity"), maxIntake);
            }
        };
    }

    public static Specification<CollegeBranch> cbHasDuration(Integer durationYears) {
        return (root, query, cb) -> durationYears == null ? null : cb.equal(root.get("durationYears"), durationYears);
    }

    // --- Cutoff Specifications ---

    public static Specification<Cutoff> cutoffHasExam(ExamName exam) {
        return (root, query, cb) -> exam == null ? null : cb.equal(root.get("examName"), exam);
    }

    public static Specification<Cutoff> cutoffHasYear(Integer year) {
        return (root, query, cb) -> year == null ? null : cb.equal(root.get("year"), year);
    }

    public static Specification<Cutoff> cutoffHasRound(Integer round) {
        return (root, query, cb) -> round == null ? null : cb.equal(root.get("round"), round);
    }

    public static Specification<Cutoff> cutoffHasCategory(Category category) {
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Cutoff> cutoffHasRawSeatType(String rawSeatType) {
        return (root, query, cb) -> (rawSeatType == null || rawSeatType.isBlank()) ? null : cb.like(cb.lower(root.get("rawSeatType")), "%" + rawSeatType.toLowerCase() + "%");
    }

    public static Specification<Cutoff> cutoffHasCollege(String collegeFilter) {
        return (root, query, cb) -> {
            if (collegeFilter == null || collegeFilter.isBlank()) return null;
            Join<Cutoff, CollegeBranch> cbJoin = root.join("collegeBranch");
            Join<CollegeBranch, College> collegeJoin = cbJoin.join("college");
            return cb.or(
                    cb.equal(cb.lower(collegeJoin.get("collegeCode")), collegeFilter.toLowerCase().trim()),
                    cb.like(cb.lower(collegeJoin.get("name")), "%" + collegeFilter.toLowerCase() + "%")
            );
        };
    }

    public static Specification<Cutoff> cutoffHasBranch(String branchFilter) {
        return (root, query, cb) -> {
            if (branchFilter == null || branchFilter.isBlank()) return null;
            Join<Cutoff, CollegeBranch> cbJoin = root.join("collegeBranch");
            Join<CollegeBranch, Branch> branchJoin = cbJoin.join("branch");
            return cb.or(
                    cb.equal(cb.lower(branchJoin.get("branchCode")), branchFilter.toLowerCase().trim()),
                    cb.like(cb.lower(branchJoin.get("name")), "%" + branchFilter.toLowerCase() + "%")
            );
        };
    }

    public static Specification<Cutoff> cutoffHasRankRange(Integer minRank, Integer maxRank) {
        return (root, query, cb) -> {
            if (minRank == null && maxRank == null) return null;
            if (minRank != null && maxRank != null) {
                return cb.between(root.get("closingRank"), minRank, maxRank);
            } else if (minRank != null) {
                return cb.greaterThanOrEqualTo(root.get("closingRank"), minRank);
            } else {
                return cb.lessThanOrEqualTo(root.get("closingRank"), maxRank);
            }
        };
    }

    public static Specification<Cutoff> cutoffHasPercentileRange(BigDecimal minPercentile, BigDecimal maxPercentile) {
        return (root, query, cb) -> {
            if (minPercentile == null && maxPercentile == null) return null;
            if (minPercentile != null && maxPercentile != null) {
                return cb.between(root.get("closingPercentile"), minPercentile, maxPercentile);
            } else if (minPercentile != null) {
                return cb.greaterThanOrEqualTo(root.get("closingPercentile"), minPercentile);
            } else {
                return cb.lessThanOrEqualTo(root.get("closingPercentile"), maxPercentile);
            }
        };
    }

    public static Specification<Cutoff> cutoffHasCollegeCity(String city) {
        return (root, query, cb) -> {
            if (city == null || city.isBlank()) return null;
            Join<Cutoff, CollegeBranch> cbJoin = root.join("collegeBranch");
            Join<CollegeBranch, College> collegeJoin = cbJoin.join("college");
            return cb.equal(cb.lower(collegeJoin.get("city")), city.toLowerCase().trim());
        };
    }

    public static Specification<Cutoff> cutoffHasCollegeState(String state) {
        return (root, query, cb) -> {
            if (state == null || state.isBlank()) return null;
            Join<Cutoff, CollegeBranch> cbJoin = root.join("collegeBranch");
            Join<CollegeBranch, College> collegeJoin = cbJoin.join("college");
            return cb.equal(cb.lower(collegeJoin.get("state")), state.toLowerCase().trim());
        };
    }

    // --- Placement Specifications ---

    public static Specification<Placement> placementHasCollege(String collegeFilter) {
        return (root, query, cb) -> {
            if (collegeFilter == null || collegeFilter.isBlank()) return null;
            Join<Placement, College> collegeJoin = root.join("college");
            return cb.or(
                    cb.equal(cb.lower(collegeJoin.get("collegeCode")), collegeFilter.toLowerCase().trim()),
                    cb.like(cb.lower(collegeJoin.get("name")), "%" + collegeFilter.toLowerCase() + "%")
            );
        };
    }

    public static Specification<Placement> placementHasYear(Integer year) {
        return (root, query, cb) -> year == null ? null : cb.equal(root.get("year"), year);
    }

    public static Specification<Placement> placementHasMinAveragePackage(BigDecimal minPkg) {
        return (root, query, cb) -> minPkg == null ? null : cb.greaterThanOrEqualTo(root.get("averagePackage"), minPkg);
    }

    public static Specification<Placement> placementHasMinHighestPackage(BigDecimal minPkg) {
        return (root, query, cb) -> minPkg == null ? null : cb.greaterThanOrEqualTo(root.get("highestPackage"), minPkg);
    }

    public static Specification<Placement> placementHasMinRatio(BigDecimal minRatio) {
        return (root, query, cb) -> minRatio == null ? null : cb.greaterThanOrEqualTo(root.get("placementRatio"), minRatio);
    }
}
