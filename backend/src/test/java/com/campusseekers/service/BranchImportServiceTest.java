package com.campusseekers.service;

import com.campusseekers.dto.ImportSummaryResponse;
import com.campusseekers.entity.Branch;
import com.campusseekers.mapper.BranchImportMapper;
import com.campusseekers.repository.BranchRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchImportServiceTest {

    @Mock
    private CsvImportService csvImportService;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BranchImportMapper branchMapper;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private BranchImportService branchImportService;

    @Test
    void importCsv_ShouldImportSuccessfully_WhenValid(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("branches.csv");
        Files.writeString(file, "branch_code,name\n0100219110,Civil Engineering");

        CSVParser parser = new CSVParser(new StringReader("branch_code,name\n0100219110,Civil Engineering"), 
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());

        when(csvImportService.parseCsv(anyString(), any(String[].class), anyLong())).thenReturn(parser);
        when(branchRepository.existsByBranchCode("0100219110")).thenReturn(false);
        when(branchMapper.toEntity(any())).thenReturn(new Branch());

        ImportSummaryResponse response = branchImportService.importCsv(file.toString(), false, false, 1024, 5);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(1, response.getRowsProcessed());
        assertEquals(1, response.getRowsInserted());
        verify(branchRepository, times(1)).saveAll(anyList());
    }

    @Test
    void importCsv_ShouldSkipDuplicates_WhenReplaceExistingFalse(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("branches.csv");
        Files.writeString(file, "branch_code,name\n0100219110,Civil Engineering");

        CSVParser parser = new CSVParser(new StringReader("branch_code,name\n0100219110,Civil Engineering"), 
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());

        when(csvImportService.parseCsv(anyString(), any(String[].class), anyLong())).thenReturn(parser);
        when(branchRepository.existsByBranchCode("0100219110")).thenReturn(true);

        ImportSummaryResponse response = branchImportService.importCsv(file.toString(), false, false, 1024, 5);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(1, response.getRowsProcessed());
        assertEquals(0, response.getRowsInserted());
        assertEquals(1, response.getRowsSkipped());
        assertEquals(1, response.getDuplicateRows());
        verify(branchRepository, never()).saveAll(anyList());
    }
}
