package com.campusseekers.specification;

import com.campusseekers.entity.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class StudentWorkflowSpecifications {

    private StudentWorkflowSpecifications() {}

    // --- Wishlist Specifications ---

    public static Specification<Wishlist> wishlistHasStudent(UUID studentProfileId) {
        return (root, query, cb) -> studentProfileId == null ? null : cb.equal(root.get("studentProfile").get("id"), studentProfileId);
    }

    public static Specification<Wishlist> wishlistNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("isDeleted"), false);
    }

    public static Specification<Wishlist> wishlistHasCollegeKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            Join<Wishlist, College> collegeJoin = root.join("college");
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(collegeJoin.get("name")), pattern),
                    cb.like(cb.lower(collegeJoin.get("collegeCode")), pattern),
                    cb.like(cb.lower(collegeJoin.get("city")), pattern),
                    cb.like(cb.lower(collegeJoin.get("state")), pattern)
            );
        };
    }

    public static Specification<Wishlist> wishlistHasNaac(String naac) {
        return (root, query, cb) -> (naac == null || naac.isBlank()) ? null : cb.equal(root.join("college").get("naacGrade"), naac.trim());
    }

    // --- Shortlist Specifications ---

    public static Specification<Shortlist> shortlistHasStudent(UUID studentProfileId) {
        return (root, query, cb) -> studentProfileId == null ? null : cb.equal(root.get("studentProfile").get("id"), studentProfileId);
    }

    public static Specification<Shortlist> shortlistNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("isDeleted"), false);
    }

    public static Specification<Shortlist> shortlistHasCollegeKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            Join<Shortlist, CollegeBranch> cbJoin = root.join("collegeBranch");
            Join<CollegeBranch, College> collegeJoin = cbJoin.join("college");
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(collegeJoin.get("name")), pattern),
                    cb.like(cb.lower(collegeJoin.get("collegeCode")), pattern)
            );
        };
    }

    public static Specification<Shortlist> shortlistHasBranchKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            Join<Shortlist, CollegeBranch> cbJoin = root.join("collegeBranch");
            Join<CollegeBranch, Branch> branchJoin = cbJoin.join("branch");
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(branchJoin.get("name")), pattern),
                    cb.like(cb.lower(branchJoin.get("branchCode")), pattern)
            );
        };
    }

    public static Specification<Shortlist> shortlistHasCity(String city) {
        return (root, query, cb) -> (city == null || city.isBlank()) ? null : cb.equal(cb.lower(root.join("collegeBranch").join("college").get("city")), city.toLowerCase().trim());
    }

    public static Specification<Shortlist> shortlistHasState(String state) {
        return (root, query, cb) -> (state == null || state.isBlank()) ? null : cb.equal(cb.lower(root.join("collegeBranch").join("college").get("state")), state.toLowerCase().trim());
    }

    public static Specification<Shortlist> shortlistHasNaac(String naac) {
        return (root, query, cb) -> (naac == null || naac.isBlank()) ? null : cb.equal(root.join("collegeBranch").join("college").get("naacGrade"), naac.trim());
    }

    public static Specification<Shortlist> shortlistHasMaxFees(BigDecimal maxFees) {
        return (root, query, cb) -> maxFees == null ? null : cb.lessThanOrEqualTo(root.join("collegeBranch").get("feesPerYear"), maxFees);
    }

    public static Specification<Shortlist> shortlistHasPriority(Integer priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Shortlist> shortlistHasStatus(AdmissionStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.join("admissionTracker").get("currentStatus"), status);
    }

    public static Specification<Shortlist> shortlistHasCreatedAfter(Instant date) {
        return (root, query, cb) -> date == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<Shortlist> shortlistHasRecommendationCategory(RecommendationCategory category, UUID studentProfileId) {
        return (root, query, cb) -> {
            if (category == null || studentProfileId == null) return null;
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<RecommendationItem> recItemRoot = subquery.from(RecommendationItem.class);
            subquery.select(recItemRoot.get("collegeBranch").get("id"));
            subquery.where(
                    cb.and(
                            cb.equal(recItemRoot.get("recommendationCategory"), category),
                            cb.equal(recItemRoot.get("recommendation").get("studentProfile").get("id"), studentProfileId)
                    )
            );
            return root.get("collegeBranch").get("id").in(subquery);
        };
    }
}
