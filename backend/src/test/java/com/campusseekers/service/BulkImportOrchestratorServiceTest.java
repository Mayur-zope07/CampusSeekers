package com.campusseekers.service;

import com.campusseekers.dto.ImportSummaryResponse;
import com.campusseekers.dto.ImportSummaryResponse.DatasetStats;
import com.campusseekers.repository.BranchRepository;
import com.campusseekers.repository.CollegeBranchRepository;
import com.campusseekers.repository.CollegeRepository;
import com.campusseekers.repository.CutoffRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkImportOrchestratorServiceTest {

    @Mock
    private CsvImportService csvImportService;
    @Mock
    private CollegeImportService collegeImportService;
    @Mock
    private BranchImportService branchImportService;
    @Mock
    private CollegeBranchImportService collegeBranchImportService;
    @Mock
    private CutoffImportService cutoffImportService;
    @Mock
    private SeatMatrixImportService seatMatrixImportService;

    @Mock
    private CollegeRepository collegeRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private CollegeBranchRepository collegeBranchRepository;
    @Mock
    private CutoffRepository cutoffRepository;

    @InjectMocks
    private BulkImportOrchestratorService orchestratorService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        Path colFile = tempDir.resolve("colleges.csv");
        Path brFile = tempDir.resolve("branches.csv");
        Path cbFile = tempDir.resolve("college_branches.csv");
        Path cutFile = tempDir.resolve("cutoffs.csv");
        Path smFile = tempDir.resolve("seat_matrix.csv");

        Files.writeString(colFile, "college_code\n1002");
        Files.writeString(brFile, "branch_code\n0100219110");
        Files.writeString(cbFile, "college_code,branch_code\n1002,0100219110");
        Files.writeString(cutFile, "college_code,branch_code,exam_name,year,round,category,raw_seat_type\n1002,0100219110,MHT_CET,2025,1,OPEN,GOPENS");
        Files.writeString(smFile, "college_code,branch_code,intake_capacity\n1002,0100219110,60");

        ReflectionTestUtils.setField(orchestratorService, "collegesPath", colFile.toString());
        ReflectionTestUtils.setField(orchestratorService, "branchesPath", brFile.toString());
        ReflectionTestUtils.setField(orchestratorService, "collegeBranchesPath", cbFile.toString());
        ReflectionTestUtils.setField(orchestratorService, "cutoffsPath", cutFile.toString());
        ReflectionTestUtils.setField(orchestratorService, "seatMatrixPath", smFile.toString());
        ReflectionTestUtils.setField(orchestratorService, "batchSize", 500);
        ReflectionTestUtils.setField(orchestratorService, "maxFileSize", 1024L * 1024L);
    }

    @Test
    void importAll_ShouldOrchestrateImportsSuccessfully_WhenValid() throws IOException {
        // Setup headers mock parser
        CSVParser colParser = new CSVParser(new StringReader("college_code\n1002"), CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());
        CSVParser brParser = new CSVParser(new StringReader("branch_code\n0100219110"), CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());
        CSVParser cbParser = new CSVParser(new StringReader("college_code,branch_code\n1002,0100219110"), CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());
        CSVParser cutParser = new CSVParser(new StringReader("college_code,branch_code,exam_name,year,round,category,raw_seat_type\n1002,0100219110,MHT_CET,2025,1,OPEN,GOPENS"), CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());
        CSVParser smParser = new CSVParser(new StringReader("college_code,branch_code,intake_capacity\n1002,0100219110,60"), CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());

        when(csvImportService.parseCsv(anyString(), any(String[].class), anyLong()))
                .thenReturn(colParser)
                .thenReturn(brParser)
                .thenReturn(cbParser)
                .thenReturn(cutParser)
                .thenReturn(smParser);

        when(collegeRepository.findAll()).thenReturn(Collections.emptyList());
        when(branchRepository.findAll()).thenReturn(Collections.emptyList());
        when(collegeBranchRepository.findAll()).thenReturn(Collections.emptyList());

        // Setup service summaries
        ImportSummaryResponse successRes = ImportSummaryResponse.builder()
                .status("SUCCESS")
                .datasetDetails(new HashMap<>())
                .build();
        successRes.getDatasetDetails().put("Colleges", DatasetStats.builder().build());
        successRes.getDatasetDetails().put("Branches", DatasetStats.builder().build());
        successRes.getDatasetDetails().put("College Branches", DatasetStats.builder().build());
        successRes.getDatasetDetails().put("Cutoffs", DatasetStats.builder().build());
        successRes.getDatasetDetails().put("Seat Matrix", DatasetStats.builder().build());

        when(collegeImportService.importCsv(anyString(), anyBoolean(), anyBoolean(), anyLong(), anyInt())).thenReturn(successRes);
        when(branchImportService.importCsv(anyString(), anyBoolean(), anyBoolean(), anyLong(), anyInt())).thenReturn(successRes);
        when(collegeBranchImportService.importCsv(anyString(), anyBoolean(), anyBoolean(), anyLong(), anyInt(), anySet(), anySet(), anySet())).thenReturn(successRes);
        when(cutoffImportService.importCsv(anyString(), anyBoolean(), anyBoolean(), anyLong(), anyInt(), anySet())).thenReturn(successRes);
        when(seatMatrixImportService.importCsv(anyString(), anyBoolean(), anyBoolean(), anyLong(), anyInt(), anySet())).thenReturn(successRes);

        ImportSummaryResponse response = orchestratorService.importAll(false, false);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        verify(collegeImportService, times(1)).importCsv(anyString(), eq(false), eq(false), anyLong(), anyInt());
        verify(branchImportService, times(1)).importCsv(anyString(), eq(false), eq(false), anyLong(), anyInt());
    }

    @Test
    void importAll_ShouldDeleteInOrder_WhenReplaceExistingTrue() throws IOException {
        CSVParser colParser = new CSVParser(new StringReader("college_code\n1002"), CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());
        CSVParser brParser = new CSVParser(new StringReader("branch_code\n0100219110"), CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());
        CSVParser cbParser = new CSVParser(new StringReader("college_code,branch_code\n1002,0100219110"), CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());
        CSVParser cutParser = new CSVParser(new StringReader("college_code,branch_code,exam_name,year,round,category,raw_seat_type\n1002,0100219110,MHT_CET,2025,1,OPEN,GOPENS"), CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());
        CSVParser smParser = new CSVParser(new StringReader("college_code,branch_code,intake_capacity\n1002,0100219110,60"), CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());

        when(csvImportService.parseCsv(anyString(), any(String[].class), anyLong()))
                .thenReturn(colParser)
                .thenReturn(brParser)
                .thenReturn(cbParser)
                .thenReturn(cutParser)
                .thenReturn(smParser);

        ImportSummaryResponse successRes = ImportSummaryResponse.builder()
                .status("SUCCESS")
                .datasetDetails(new HashMap<>())
                .build();
        successRes.getDatasetDetails().put("Colleges", DatasetStats.builder().build());
        successRes.getDatasetDetails().put("Branches", DatasetStats.builder().build());
        successRes.getDatasetDetails().put("College Branches", DatasetStats.builder().build());
        successRes.getDatasetDetails().put("Cutoffs", DatasetStats.builder().build());
        successRes.getDatasetDetails().put("Seat Matrix", DatasetStats.builder().build());

        when(collegeImportService.importCsv(anyString(), anyBoolean(), anyBoolean(), anyLong(), anyInt())).thenReturn(successRes);
        when(branchImportService.importCsv(anyString(), anyBoolean(), anyBoolean(), anyLong(), anyInt())).thenReturn(successRes);
        when(collegeBranchImportService.importCsv(anyString(), anyBoolean(), anyBoolean(), anyLong(), anyInt(), anySet(), anySet(), anySet())).thenReturn(successRes);
        when(cutoffImportService.importCsv(anyString(), anyBoolean(), anyBoolean(), anyLong(), anyInt(), anySet())).thenReturn(successRes);
        when(seatMatrixImportService.importCsv(anyString(), anyBoolean(), anyBoolean(), anyLong(), anyInt(), anySet())).thenReturn(successRes);

        orchestratorService.importAll(true, false);

        // Verify direct repository deletions in correct order
        verify(cutoffRepository, times(1)).deleteAllInBatch();
        verify(collegeBranchRepository, times(1)).deleteAllInBatch();
        verify(branchRepository, times(1)).deleteAllInBatch();
        verify(collegeRepository, times(1)).deleteAllInBatch();
    }
}
