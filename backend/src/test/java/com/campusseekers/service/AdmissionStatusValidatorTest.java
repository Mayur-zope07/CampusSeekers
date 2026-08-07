package com.campusseekers.service;

import com.campusseekers.entity.AdmissionStatus;
import com.campusseekers.exception.InvalidStateTransitionException;
import com.campusseekers.validation.AdmissionStatusValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdmissionStatusValidatorTest {

    private AdmissionStatusValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AdmissionStatusValidator();
    }

    @Test
    void validateTransition_ShouldAllowValidTransitions() {
        assertDoesNotThrow(() -> validator.validateTransition(AdmissionStatus.INTERESTED, AdmissionStatus.APPLIED));
        assertDoesNotThrow(() -> validator.validateTransition(AdmissionStatus.APPLIED, AdmissionStatus.DOCUMENTS_UPLOADED));
        assertDoesNotThrow(() -> validator.validateTransition(AdmissionStatus.DOCUMENTS_UPLOADED, AdmissionStatus.DOCUMENTS_VERIFIED));
        assertDoesNotThrow(() -> validator.validateTransition(AdmissionStatus.DOCUMENTS_VERIFIED, AdmissionStatus.SEAT_ALLOTTED));
        assertDoesNotThrow(() -> validator.validateTransition(AdmissionStatus.SEAT_ALLOTTED, AdmissionStatus.CONFIRMED));
    }

    @Test
    void validateTransition_ShouldAllowExits() {
        assertDoesNotThrow(() -> validator.validateTransition(AdmissionStatus.INTERESTED, AdmissionStatus.WITHDRAWN));
        assertDoesNotThrow(() -> validator.validateTransition(AdmissionStatus.APPLIED, AdmissionStatus.REJECTED));
        assertDoesNotThrow(() -> validator.validateTransition(AdmissionStatus.DOCUMENTS_UPLOADED, AdmissionStatus.WITHDRAWN));
        assertDoesNotThrow(() -> validator.validateTransition(AdmissionStatus.DOCUMENTS_VERIFIED, AdmissionStatus.REJECTED));
        assertDoesNotThrow(() -> validator.validateTransition(AdmissionStatus.SEAT_ALLOTTED, AdmissionStatus.WITHDRAWN));
    }

    @Test
    void validateTransition_ShouldAllowSameStatusTransition() {
        assertDoesNotThrow(() -> validator.validateTransition(AdmissionStatus.INTERESTED, AdmissionStatus.INTERESTED));
    }

    @Test
    void validateTransition_ShouldRejectInvalidTransitions() {
        assertThrows(InvalidStateTransitionException.class, () -> validator.validateTransition(AdmissionStatus.INTERESTED, AdmissionStatus.CONFIRMED));
        assertThrows(InvalidStateTransitionException.class, () -> validator.validateTransition(AdmissionStatus.APPLIED, AdmissionStatus.SEAT_ALLOTTED));
    }

    @Test
    void validateTransition_ShouldRejectTransitionsFromTerminalStates() {
        assertThrows(InvalidStateTransitionException.class, () -> validator.validateTransition(AdmissionStatus.CONFIRMED, AdmissionStatus.INTERESTED));
        assertThrows(InvalidStateTransitionException.class, () -> validator.validateTransition(AdmissionStatus.REJECTED, AdmissionStatus.APPLIED));
        assertThrows(InvalidStateTransitionException.class, () -> validator.validateTransition(AdmissionStatus.WITHDRAWN, AdmissionStatus.INTERESTED));
    }
}
