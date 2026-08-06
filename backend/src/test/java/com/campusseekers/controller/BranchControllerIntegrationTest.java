package com.campusseekers.controller;

import com.campusseekers.dto.BranchRequest;
import com.campusseekers.dto.BranchResponse;
import com.campusseekers.service.BranchSearchService;
import com.campusseekers.service.BranchService;
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
class BranchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BranchService branchService;

    @MockBean
    private BranchSearchService branchSearchService;

    @Test
    void getBranches_ShouldReturnUnauthorized_WhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/branches"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void getBranches_ShouldReturnList_WhenStudent() throws Exception {
        BranchResponse mockResponse = BranchResponse.builder()
                .id(UUID.randomUUID())
                .name("Computer Science")
                .branchCode("CS")
                .build();

        com.campusseekers.dto.PageResponse<BranchResponse> mockPage = com.campusseekers.dto.PageResponse.from(
                new org.springframework.data.domain.PageImpl<>(List.of(mockResponse))
        );
        when(branchSearchService.searchBranches(any(), any(), any(), any())).thenReturn(mockPage);

        mockMvc.perform(get("/api/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].branchCode").value("CS"));
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void postBranch_ShouldReturnForbidden_WhenStudent() throws Exception {
        BranchRequest request = BranchRequest.builder()
                .name("Computer Science")
                .branchCode("CS")
                .build();

        mockMvc.perform(post("/api/admin/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void postBranch_ShouldReturnCreated_WhenAdminAndPayloadIsValid() throws Exception {
        BranchRequest request = BranchRequest.builder()
                .name("Computer Science")
                .branchCode("CS")
                .build();

        BranchResponse response = BranchResponse.builder()
                .branchCode("CS")
                .build();

        when(branchService.createBranch(any(BranchRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Branch created successfully"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void deleteBranch_ShouldReturnOk_WhenAdmin() throws Exception {
        UUID branchId = UUID.randomUUID();
        doNothing().when(branchService).deleteBranch(branchId);

        mockMvc.perform(delete("/api/admin/branches/" + branchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Branch deleted successfully"));

        verify(branchService, times(1)).deleteBranch(branchId);
    }
}
