package com.campusseekers.service;

import com.campusseekers.dto.ImportSummaryResponse;
import com.campusseekers.entity.Branch;
import com.campusseekers.entity.College;
import com.campusseekers.entity.CollegeBranch;
import com.campusseekers.mapper.CollegeBranchImportMapper;
import com.campusseekers.repository.BranchRepository;
import com.campusseekers.repository.CollegeBranchRepository;
import com.campusseekers.repository.CollegeRepository;
import jakarta.persistence.EntityManager;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollegeBranchImportServiceTest {

    @Mock
    private CsvImportService csvImportService;

    @Mock
    private CollegeBranchRepository collegeBranchRepository;

    @Mock
    private CollegeRepository collegeRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private CollegeBranchImportMapper collegeBranchMapper;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CollegeBranchImportService collegeBranchImportService;

    @Test
    void importCsv_ShouldImportSuccessfully_WhenCollegeAndBranchExist(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("college_branches.csv");
        Files.writeString(file, "college_code,branch_code,intake_capacity,fees_per_year,duration_years\n1002,0100219110,60,50000.00,4");

        CSVParser parser = new CSVParser(new StringReader("college_code,branch_code,intake_capacity,fees_per_year,duration_years\n1002,0100219110,60,50000.00,4"), 
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());

        College college = new College();
        college.setId(UUID.randomUUID());
        college.setCollegeCode("1002");

        Branch branch = new Branch();
        branch.setId(UUID.randomUUID());
        branch.setBranchCode("0100219110");

        when(csvImportService.parseCsv(anyString(), any(String[].class), anyLong())).thenReturn(parser);
        when(collegeRepository.findAll()).thenReturn(Collections.singletonList(college));
        when(branchRepository.findAll()).thenReturn(Collections.singletonList(branch));
        when(collegeBranchRepository.existsByCollegeIdAndBranchId(college.getId(), branch.getId())).thenReturn(false);
        when(collegeBranchMapper.toEntity(any(), any(), any())).thenReturn(new CollegeBranch());

        ImportSummaryResponse response = collegeBranchImportService.importCsv(file.toString(), false, false, 1024, 5, null, null, null);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(1, response.getRowsProcessed());
        assertEquals(1, response.getRowsInserted());
        verify(collegeBranchRepository, times(1)).saveAll(anyList());
    }

    @Test
    void importCsv_ShouldThrowException_WhenParentCollegeMissing(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("college_branches.csv");
        Files.writeString(file, "college_code,branch_code,intake_capacity,fees_per_year,duration_years\n1002,0100219110,60,50000.00,4");

        CSVParser parser = new CSVParser(new StringReader("college_code,branch_code,intake_capacity,fees_per_year,duration_years\n1002,0100219110,60,50000.00,4"), 
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());

        when(csvImportService.parseCsv(anyString(), any(String[].class), anyLong())).thenReturn(parser);
        when(collegeRepository.findAll()).thenReturn(Collections.emptyList());
        when(branchRepository.findAll()).thenReturn(Collections.singletonList(new Branch()));

        assertThrows(IllegalArgumentException.class, () ->
                collegeBranchImportService.importCsv(file.toString(), false, false, 1024, 5, null, null, null)
        );
    }
}
