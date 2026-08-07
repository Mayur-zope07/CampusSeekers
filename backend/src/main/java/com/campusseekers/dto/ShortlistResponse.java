package com.campusseekers.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ShortlistResponse(
        UUID id,
        UUID studentProfileId,
        UUID collegeBranchId,
        UUID collegeId,
        String collegeCode,
        String collegeName,
        UUID branchId,
        String branchCode,
        String branchName,
        String city,
        String state,
        String naacGrade,
        BigDecimal feesPerYear,
        Integer priority,
        String notes,
        Boolean isDeleted,
        Instant addedAt,
        AdmissionTrackerResponse tracker
) {}
