package com.campusseekers.service;

import com.campusseekers.dto.ImportSummaryResponse;
import com.campusseekers.entity.Branch;
import com.campusseekers.entity.College;
import com.campusseekers.entity.CollegeBranch;
import com.campusseekers.mapper.SeatMatrixImportMapper;
import com.campusseekers.repository.CollegeBranchRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatMatrixImportServiceTest {

    @Mock
    private CsvImportService csvImportService;

    @Mock
    private CollegeBranchRepository collegeBranchRepository;

    @Mock
    private SeatMatrixImportMapper seatMatrixMapper;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private SeatMatrixImportService seatMatrixImportService;

    @Test
    void importCsv_ShouldUpdateSuccessfully_WhenCollegeBranchExists(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("seat_matrix.csv");
        Files.writeString(file, "college_code,branch_code,intake_capacity\n1002,0100219110,60");

        CSVParser parser = new CSVParser(new StringReader("college_code,branch_code,intake_capacity\n1002,0100219110,60"), 
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());

        College college = new College();
        college.setCollegeCode("1002");

        Branch branch = new Branch();
        branch.setBranchCode("0100219110");

        CollegeBranch cb = new CollegeBranch();
        cb.setId(UUID.randomUUID());
        cb.setCollege(college);
        cb.setBranch(branch);

        when(csvImportService.parseCsv(anyString(), any(String[].class), anyLong())).thenReturn(parser);
        when(collegeBranchRepository.findAll()).thenReturn(Collections.singletonList(cb));

        ImportSummaryResponse response = seatMatrixImportService.importCsv(file.toString(), false, false, 1024, 5, null);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(1, response.getRowsProcessed());
        assertEquals(1, response.getRowsUpdated());
        verify(collegeBranchRepository, times(1)).saveAll(anyList());
    }
}
