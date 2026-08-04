package com.campusseekers.service;

import com.campusseekers.dto.BranchResponse;
import com.campusseekers.dto.BranchRequest;

import java.util.List;
import java.util.UUID;

public interface BranchService {
    BranchResponse createBranch(BranchRequest request);
    BranchResponse updateBranch(UUID id, BranchRequest request);
    void deleteBranch(UUID id);
    BranchResponse getBranchById(UUID id);
    List<BranchResponse> getAllBranches();
}
