package com.campusseekers.service;

import com.campusseekers.dto.RecommendationRequest;
import com.campusseekers.entity.CollegeType;
import com.campusseekers.entity.Cutoff;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationFilterService {

    public List<Cutoff> filter(List<Cutoff> cutoffs, RecommendationRequest request) {
        if (cutoffs == null) {
            return List.of();
        }

        return cutoffs.stream()
                .filter(cutoff -> filterByExam(cutoff, request))
                .filter(cutoff -> filterByYear(cutoff, request))
                .filter(cutoff -> filterByCategory(cutoff, request))
                .filter(cutoff -> filterByBranches(cutoff, request))
                .filter(cutoff -> filterByCities(cutoff, request))
                .filter(cutoff -> filterByCollegeTypes(cutoff, request))
                .filter(cutoff -> filterByFees(cutoff, request))
                .filter(cutoff -> filterByNaac(cutoff, request))
                .collect(Collectors.toList());
    }

    private boolean filterByExam(Cutoff cutoff, RecommendationRequest request) {
        return cutoff.getExamName() == request.exam();
    }

    private boolean filterByYear(Cutoff cutoff, RecommendationRequest request) {
        return cutoff.getYear().equals(request.year());
    }

    private boolean filterByCategory(Cutoff cutoff, RecommendationRequest request) {
        return cutoff.getCategory() == request.category();
    }

    private boolean filterByBranches(Cutoff cutoff, RecommendationRequest request) {
        if (request.preferredBranches() == null || request.preferredBranches().isEmpty()) {
            return true;
        }
        String branchCode = cutoff.getCollegeBranch().getBranch().getBranchCode();
        String branchName = cutoff.getCollegeBranch().getBranch().getName();
        return request.preferredBranches().stream().anyMatch(pref ->
                (branchCode != null && branchCode.equalsIgnoreCase(pref.trim())) ||
                (branchName != null && branchName.equalsIgnoreCase(pref.trim()))
        );
    }

    private boolean filterByCities(Cutoff cutoff, RecommendationRequest request) {
        if (request.preferredCities() == null || request.preferredCities().isEmpty()) {
            return true;
        }
        String city = cutoff.getCollegeBranch().getCollege().getCity();
        return request.preferredCities().stream().anyMatch(pref ->
                city != null && city.equalsIgnoreCase(pref.trim())
        );
    }

    private boolean filterByCollegeTypes(Cutoff cutoff, RecommendationRequest request) {
        if (request.preferredCollegeTypes() == null || request.preferredCollegeTypes().isEmpty()) {
            return true;
        }
        CollegeType type = cutoff.getCollegeBranch().getCollege().getCollegeType();
        return request.preferredCollegeTypes().contains(type);
    }

    private boolean filterByFees(Cutoff cutoff, RecommendationRequest request) {
        if (request.maximumFees() == null) {
            return true;
        }
        BigDecimal fees = cutoff.getCollegeBranch().getFeesPerYear();
        return fees == null || fees.compareTo(request.maximumFees()) <= 0;
    }

    private boolean filterByNaac(Cutoff cutoff, RecommendationRequest request) {
        if (request.minimumNAAC() == null || request.minimumNAAC().isBlank()) {
            return true;
        }
        String collegeNaac = cutoff.getCollegeBranch().getCollege().getNaacGrade();
        return getNaacScore(collegeNaac) >= getNaacScore(request.minimumNAAC());
    }

    private int getNaacScore(String naac) {
        if (naac == null || naac.isBlank()) {
            return 0;
        }
        switch (naac.trim().toUpperCase()) {
            case "A++": return 8;
            case "A+": return 7;
            case "A": return 6;
            case "B++": return 5;
            case "B+": return 4;
            case "B": return 3;
            case "C": return 2;
            case "D": return 1;
            default: return 0;
        }
    }
}
