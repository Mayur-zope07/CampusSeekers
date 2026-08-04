package com.campusseekers.service;

import com.campusseekers.dto.CollegeRequest;
import com.campusseekers.dto.CollegeResponse;

import java.util.List;
import java.util.UUID;

public interface CollegeService {
    CollegeResponse createCollege(CollegeRequest request);
    CollegeResponse updateCollege(UUID id, CollegeRequest request);
    void deleteCollege(UUID id);
    CollegeResponse getCollegeById(UUID id);
    List<CollegeResponse> getAllColleges();
}
