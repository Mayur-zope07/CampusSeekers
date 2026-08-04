package com.campusseekers.controller;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.ExamScoreRequest;
import com.campusseekers.dto.ExamScoreResponse;
import com.campusseekers.dto.StudentProfileRequest;
import com.campusseekers.dto.StudentProfileResponse;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.ExamName;
import com.campusseekers.entity.Gender;
import com.campusseekers.service.ExamScoreService;
import com.campusseekers.service.StudentProfileService;
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

import java.math.BigDecimal;
import java.time.Instant;
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
class StudentProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentProfileService studentProfileService;

    @MockBean
    private ExamScoreService examScoreService;

    @Test
    void endpoints_ShouldReturnUnauthorized_WhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/profile/scores"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getProfile_ShouldReturnProfile_WhenAuthenticated() throws Exception {
        StudentProfileResponse mockResponse = StudentProfileResponse.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .phone("9876543210")
                .gender(Gender.MALE)
                .category(Category.OPEN)
                .homeState("State")
                .homeDistrict("District")
                .createdAt(Instant.now())
                .build();

        when(studentProfileService.getCurrentProfile()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.category").value("OPEN"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void createProfile_ShouldReturnCreated_WhenPayloadIsValid() throws Exception {
        StudentProfileRequest request = StudentProfileRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .phone("9876543210")
                .gender(Gender.MALE)
                .category(Category.OPEN)
                .homeState("State")
                .homeDistrict("District")
                .build();

        StudentProfileResponse mockResponse = StudentProfileResponse.builder()
                .firstName("John")
                .build();

        when(studentProfileService.createProfile(any(StudentProfileRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student profile created successfully"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void createProfile_ShouldReturnBadRequest_WhenPhoneIsInvalid() throws Exception {
        StudentProfileRequest request = StudentProfileRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .phone("12345") // Invalid (needs 10 digits)
                .gender(Gender.MALE)
                .category(Category.OPEN)
                .homeState("State")
                .homeDistrict("District")
                .build();

        mockMvc.perform(post("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.phone").exists());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getScores_ShouldReturnList_WhenAuthenticated() throws Exception {
        ExamScoreResponse scoreResponse = ExamScoreResponse.builder()
                .id(UUID.randomUUID())
                .examName(ExamName.MHT_CET)
                .examYear(2026)
                .rank(45)
                .percentile(new BigDecimal("99.85"))
                .build();

        when(examScoreService.getScores()).thenReturn(List.of(scoreResponse));

        mockMvc.perform(get("/api/profile/scores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].examName").value("MHT_CET"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void addScore_ShouldReturnCreated_WhenPayloadIsValid() throws Exception {
        ExamScoreRequest request = ExamScoreRequest.builder()
                .examName(ExamName.MHT_CET)
                .examYear(2026)
                .rank(45)
                .percentile(new BigDecimal("99.85"))
                .marks(185)
                .build();

        ExamScoreResponse mockResponse = ExamScoreResponse.builder()
                .examName(ExamName.MHT_CET)
                .build();

        when(examScoreService.addScore(any(ExamScoreRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/profile/scores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Exam score registered successfully"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void deleteScore_ShouldReturnOk() throws Exception {
        UUID scoreId = UUID.randomUUID();
        doNothing().when(examScoreService).deleteScore(scoreId);

        mockMvc.perform(delete("/api/profile/scores/" + scoreId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Exam score deleted successfully"));

        verify(examScoreService, times(1)).deleteScore(scoreId);
    }
}
