package com.campusseekers.controller;

import com.campusseekers.dto.CollegeRequest;
import com.campusseekers.dto.CollegeResponse;
import com.campusseekers.entity.CollegeStatus;
import com.campusseekers.entity.CollegeType;
import com.campusseekers.service.CollegeService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CollegeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CollegeService collegeService;

    @Test
    void getEndpoints_ShouldReturnUnauthorized_WhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/colleges"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getColleges_ShouldReturnList_WhenStudent() throws Exception {
        CollegeResponse mockResponse = CollegeResponse.builder()
                .id(UUID.randomUUID())
                .name("COEP")
                .collegeCode("COEP001")
                .collegeType(CollegeType.GOVERNMENT)
                .establishmentYear(1854)
                .city("Pune")
                .state("Maharashtra")
                .status(CollegeStatus.ACTIVE)
                .build();

        when(collegeService.getAllColleges()).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/api/colleges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("COEP"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void postCollege_ShouldReturnForbidden_WhenStudent() throws Exception {
        CollegeRequest request = CollegeRequest.builder()
                .name("COEP")
                .collegeCode("COEP001")
                .collegeType(CollegeType.GOVERNMENT)
                .establishmentYear(1854)
                .city("Pune")
                .state("Maharashtra")
                .nbaAccredited(true)
                .status(CollegeStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/admin/colleges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void postCollege_ShouldReturnCreated_WhenAdminAndPayloadIsValid() throws Exception {
        CollegeRequest request = CollegeRequest.builder()
                .name("COEP")
                .collegeCode("COEP001")
                .collegeType(CollegeType.GOVERNMENT)
                .establishmentYear(1854)
                .city("Pune")
                .state("Maharashtra")
                .nbaAccredited(true)
                .status(CollegeStatus.ACTIVE)
                .build();

        CollegeResponse response = CollegeResponse.builder()
                .name("COEP")
                .build();

        when(collegeService.createCollege(any(CollegeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/colleges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("College created successfully"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void updateCollege_ShouldReturnOk_WhenAdmin() throws Exception {
        UUID collegeId = UUID.randomUUID();
        CollegeRequest request = CollegeRequest.builder()
                .name("COEP Updated")
                .collegeCode("COEP001")
                .collegeType(CollegeType.GOVERNMENT)
                .establishmentYear(1854)
                .city("Pune")
                .state("Maharashtra")
                .nbaAccredited(true)
                .status(CollegeStatus.ACTIVE)
                .build();

        CollegeResponse response = CollegeResponse.builder()
                .name("COEP Updated")
                .build();

        when(collegeService.updateCollege(eq(collegeId), any(CollegeRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/admin/colleges/" + collegeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void deleteCollege_ShouldReturnOk_WhenAdmin() throws Exception {
        UUID collegeId = UUID.randomUUID();
        doNothing().when(collegeService).deleteCollege(collegeId);

        mockMvc.perform(delete("/api/admin/colleges/" + collegeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("College deleted successfully"));

        verify(collegeService, times(1)).deleteCollege(collegeId);
    }
}
