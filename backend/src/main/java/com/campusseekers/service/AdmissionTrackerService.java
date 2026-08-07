package com.campusseekers.service;

import com.campusseekers.dto.AdmissionTrackerRequest;
import com.campusseekers.dto.AdmissionTrackerResponse;
import com.campusseekers.dto.AdmissionTrackerHistoryResponse;

import java.util.List;
import java.util.UUID;

public interface AdmissionTrackerService {
    AdmissionTrackerResponse updateStatus(UUID trackerId, AdmissionTrackerRequest request);
    List<AdmissionTrackerHistoryResponse> getHistory(UUID trackerId);
    List<AdmissionTrackerResponse> getAdmissionTrackers();
}
