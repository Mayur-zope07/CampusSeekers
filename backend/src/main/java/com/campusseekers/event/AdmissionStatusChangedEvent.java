package com.campusseekers.event;

import com.campusseekers.entity.AdmissionStatus;
import java.time.Instant;
import java.util.UUID;

public record AdmissionStatusChangedEvent(
        UUID trackerId,
        UUID studentProfileId,
        AdmissionStatus previousStatus,
        AdmissionStatus newStatus,
        Instant timestamp
) {}
