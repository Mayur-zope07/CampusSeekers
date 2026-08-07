package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.AdmissionTrackerRequest;
import com.campusseekers.dto.AdmissionTrackerResponse;
import com.campusseekers.dto.AdmissionTrackerHistoryResponse;
import com.campusseekers.entity.AdmissionStatus;
import com.campusseekers.service.AdmissionTrackerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdmissionTrackerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdmissionTrackerService admissionTrackerService;

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void updateStatus_ShouldReturnOk_WhenTransitionIsValid() throws Exception {
        UUID trackerId = UUID.randomUUID();
        AdmissionTrackerRequest request = new AdmissionTrackerRequest(AdmissionStatus.APPLIED, "remarks");

        AdmissionTrackerResponse mockResponse = new AdmissionTrackerResponse(
                trackerId, UUID.randomUUID(), AdmissionStatus.APPLIED, "remarks", Instant.now(), Instant.now()
        );

        when(admissionTrackerService.updateStatus(eq(trackerId), any(AdmissionTrackerRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(put("/api/admission-tracker/" + trackerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentStatus").value("APPLIED"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getTimelineHistory_ShouldReturnHistory_WhenAuthorized() throws Exception {
        UUID trackerId = UUID.randomUUID();

        AdmissionTrackerHistoryResponse mockHistory = new AdmissionTrackerHistoryResponse(
                UUID.randomUUID(), trackerId, AdmissionStatus.INTERESTED, AdmissionStatus.APPLIED, "remarks", Instant.now()
        );

        when(admissionTrackerService.getHistory(trackerId)).thenReturn(List.of(mockHistory));

        mockMvc.perform(get("/api/admission-tracker/" + trackerId + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].newStatus").value("APPLIED"));
    }
}
