package com.campusseekers.controller;

import com.campusseekers.dto.ImportSummaryResponse;
import com.campusseekers.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BulkImportOrchestratorService orchestratorService;
    @MockBean
    private CollegeImportService collegeImportService;
    @MockBean
    private BranchImportService branchImportService;
    @MockBean
    private CollegeBranchImportService collegeBranchImportService;
    @MockBean
    private CutoffImportService cutoffImportService;
    @MockBean
    private SeatMatrixImportService seatMatrixImportService;

    @Test
    void importAll_ShouldReturnUnauthorized_WhenTokenIsMissing() throws Exception {
        mockMvc.perform(post("/api/admin/import/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void importAll_ShouldReturnForbidden_WhenRoleIsStudent() throws Exception {
        mockMvc.perform(post("/api/admin/import/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void importAll_ShouldReturnOk_WhenRoleIsAdminAndValid() throws Exception {
        ImportSummaryResponse mockResponse = ImportSummaryResponse.builder()
                .status("SUCCESS")
                .datasetsImported(5)
                .rowsProcessed(100)
                .rowsInserted(100)
                .build();

        when(orchestratorService.importAll(anyBoolean(), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/admin/import/all?replaceExisting=true&dryRun=false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.rowsProcessed").value(100));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void importColleges_ShouldReturnOk_WhenRoleIsAdmin() throws Exception {
        ImportSummaryResponse mockResponse = ImportSummaryResponse.builder()
                .status("SUCCESS")
                .rowsProcessed(50)
                .rowsInserted(50)
                .build();

        when(collegeImportService.importCsv(anyString(), anyBoolean(), anyBoolean(), anyLong(), anyInt())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/admin/import/colleges?replaceExisting=false&dryRun=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rowsProcessed").value(50));
    }
}
