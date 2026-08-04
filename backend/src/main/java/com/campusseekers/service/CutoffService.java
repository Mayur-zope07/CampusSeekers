package com.campusseekers.service;

import com.campusseekers.dto.CutoffRequest;
import com.campusseekers.dto.CutoffResponse;

import java.util.List;
import java.util.UUID;

public interface CutoffService {
    CutoffResponse createCutoff(CutoffRequest request);
    CutoffResponse updateCutoff(UUID id, CutoffRequest request);
    void deleteCutoff(UUID id);
    CutoffResponse getCutoffById(UUID id);
    List<CutoffResponse> getAllCutoffs();
    List<CutoffResponse> getCutoffsByCollegeBranch(UUID collegeBranchId);
}
