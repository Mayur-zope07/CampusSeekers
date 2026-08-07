package com.campusseekers.validation;

import com.campusseekers.entity.AdmissionStatus;
import com.campusseekers.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AdmissionStatusValidator {

    private static final Set<AdmissionStatus> TERMINAL_STATES = Set.of(
            AdmissionStatus.CONFIRMED,
            AdmissionStatus.REJECTED,
            AdmissionStatus.WITHDRAWN
    );

    public void validateTransition(AdmissionStatus current, AdmissionStatus next) {
        if (current == next) {
            return;
        }

        if (TERMINAL_STATES.contains(current)) {
            throw new InvalidStateTransitionException(
                    "Cannot transition from terminal state: " + current + " to " + next
            );
        }

        boolean valid = switch (current) {
            case INTERESTED -> next == AdmissionStatus.APPLIED || next == AdmissionStatus.WITHDRAWN;
            case APPLIED -> next == AdmissionStatus.DOCUMENTS_UPLOADED || next == AdmissionStatus.REJECTED || next == AdmissionStatus.WITHDRAWN;
            case DOCUMENTS_UPLOADED -> next == AdmissionStatus.DOCUMENTS_VERIFIED || next == AdmissionStatus.REJECTED || next == AdmissionStatus.WITHDRAWN;
            case DOCUMENTS_VERIFIED -> next == AdmissionStatus.SEAT_ALLOTTED || next == AdmissionStatus.REJECTED || next == AdmissionStatus.WITHDRAWN;
            case SEAT_ALLOTTED -> next == AdmissionStatus.CONFIRMED || next == AdmissionStatus.REJECTED || next == AdmissionStatus.WITHDRAWN;
            default -> false;
        };

        if (!valid) {
            throw new InvalidStateTransitionException(
                    "Invalid transition from " + current + " to " + next
            );
        }
    }
}
