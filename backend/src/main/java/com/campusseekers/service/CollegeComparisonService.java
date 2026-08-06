package com.campusseekers.service;

import com.campusseekers.dto.CollegeBranchResponse;
import com.campusseekers.dto.ComparisonResponse;
import com.campusseekers.dto.ComparisonResponse.CollegeComparisonDetail;
import com.campusseekers.dto.CutoffResponse;
import com.campusseekers.dto.PlacementResponse;
import com.campusseekers.entity.College;
import com.campusseekers.entity.CollegeBranch;
import com.campusseekers.entity.Cutoff;
import com.campusseekers.mapper.CollegeBranchMapper;
import com.campusseekers.mapper.CutoffMapper;
import com.campusseekers.mapper.PlacementMapper;
import com.campusseekers.repository.CollegeRepository;
import com.campusseekers.repository.CutoffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollegeComparisonService {

    private final CollegeRepository collegeRepository;
    private final CutoffRepository cutoffRepository;
    private final CollegeBranchMapper collegeBranchMapper;
    private final PlacementMapper placementMapper;
    private final CutoffMapper cutoffMapper;

    @Transactional(readOnly = true)
    public ComparisonResponse compareColleges(List<UUID> collegeIds) {
        if (collegeIds == null || collegeIds.size() < 2 || collegeIds.size() > 5) {
            throw new IllegalArgumentException("Comparison list must contain between 2 and 5 colleges");
        }

        log.info("Comparing colleges with IDs: {}", collegeIds);
        List<College> colleges = collegeRepository.findAllById(collegeIds);

        List<CollegeComparisonDetail> details = new ArrayList<>();
        Integer maxYear = cutoffRepository.findMaxYear().orElse(2025);

        for (College col : colleges) {
            BigDecimal minFees = null;
            BigDecimal maxFees = null;
            
            for (CollegeBranch cb : col.getCollegeBranches()) {
                BigDecimal fees = cb.getFeesPerYear();
                if (fees != null) {
                    if (minFees == null || fees.compareTo(minFees) < 0) {
                        minFees = fees;
                    }
                    if (maxFees == null || fees.compareTo(maxFees) > 0) {
                        maxFees = fees;
                    }
                }
            }

            List<CollegeBranchResponse> branchResponses = collegeBranchMapper.toResponseList(col.getCollegeBranches());
            List<PlacementResponse> placementResponses = placementMapper.toResponseList(col.getPlacements());

            List<CutoffResponse> cutoffResponses = new ArrayList<>();
            for (CollegeBranch cb : col.getCollegeBranches()) {
                List<Cutoff> cutoffs = cutoffRepository.findByCollegeBranchIdAndYear(cb.getId(), maxYear);
                cutoffResponses.addAll(cutoffMapper.toResponseList(cutoffs));
            }

            CollegeComparisonDetail detail = CollegeComparisonDetail.builder()
                    .id(col.getId())
                    .name(col.getName())
                    .collegeCode(col.getCollegeCode())
                    .collegeType(col.getCollegeType())
                    .establishmentYear(col.getEstablishmentYear())
                    .city(col.getCity())
                    .state(col.getState())
                    .website(col.getWebsite())
                    .naacGrade(col.getNaacGrade())
                    .nbaAccredited(col.getNbaAccredited())
                    .logoUrl(col.getLogoUrl())
                    .minFees(minFees)
                    .maxFees(maxFees)
                    .placements(placementResponses)
                    .branches(branchResponses)
                    .latestCutoffs(cutoffResponses)
                    .build();
            details.add(detail);
        }

        return new ComparisonResponse(details);
    }
}
