package com.campusseekers.service;

import com.campusseekers.dto.CollegeBranchRequest;
import com.campusseekers.dto.CollegeBranchResponse;

import java.util.List;
import java.util.UUID;

public interface CollegeBranchService {
    CollegeBranchResponse createCollegeBranch(CollegeBranchRequest request);
    CollegeBranchResponse updateCollegeBranch(UUID id, CollegeBranchRequest request);
    void deleteCollegeBranch(UUID id);
    CollegeBranchResponse getCollegeBranchById(UUID id);
    List<CollegeBranchResponse> getAllCollegeBranches();
    List<CollegeBranchResponse> getCollegeBranchesByCollege(UUID collegeId);
    List<CollegeBranchResponse> getCollegeBranchesByBranch(UUID branchId);
}
