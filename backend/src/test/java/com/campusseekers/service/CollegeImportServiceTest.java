package com.campusseekers.service;

import com.campusseekers.dto.ImportSummaryResponse;
import com.campusseekers.entity.College;
import com.campusseekers.mapper.CollegeImportMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollegeImportServiceTest {

    @Mock
    private CsvImportService csvImportService;

    @Mock
    private CollegeRepository collegeRepository;

    @Mock
    private CollegeImportMapper collegeMapper;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CollegeImportService collegeImportService;

    @Test
    void importCsv_ShouldImportSuccessfully_WhenValid(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("colleges.csv");
        Files.writeString(file, "college_code,name,college_type,establishment_year,city,state,website,naac_grade,nba_accredited,campus_size,logo_url,status\n1002,GCOEA,GOVERNMENT,1964,Amravati,Maharashtra,gcoea.ac.in,A,true,20,logo.png,ACTIVE");

        CSVParser parser = new CSVParser(new StringReader("college_code,name,college_type,establishment_year,city,state,website,naac_grade,nba_accredited,campus_size,logo_url,status\n1002,GCOEA,GOVERNMENT,1964,Amravati,Maharashtra,gcoea.ac.in,A,true,20,logo.png,ACTIVE"), 
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());

        when(csvImportService.parseCsv(anyString(), any(String[].class), anyLong())).thenReturn(parser);
        when(collegeRepository.existsByCollegeCode("1002")).thenReturn(false);
        when(collegeMapper.toEntity(any())).thenReturn(new College());

        ImportSummaryResponse response = collegeImportService.importCsv(file.toString(), false, false, 1024, 5);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(1, response.getRowsProcessed());
        assertEquals(1, response.getRowsInserted());
        verify(collegeRepository, times(1)).saveAll(anyList());
    }

    @Test
    void importCsv_ShouldSkipDuplicates_WhenReplaceExistingFalse(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("colleges.csv");
        Files.writeString(file, "college_code,name,college_type,establishment_year,city,state,website,naac_grade,nba_accredited,campus_size,logo_url,status\n1002,GCOEA,GOVERNMENT,1964,Amravati,Maharashtra,gcoea.ac.in,A,true,20,logo.png,ACTIVE");

        CSVParser parser = new CSVParser(new StringReader("college_code,name,college_type,establishment_year,city,state,website,naac_grade,nba_accredited,campus_size,logo_url,status\n1002,GCOEA,GOVERNMENT,1964,Amravati,Maharashtra,gcoea.ac.in,A,true,20,logo.png,ACTIVE"), 
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());

        when(csvImportService.parseCsv(anyString(), any(String[].class), anyLong())).thenReturn(parser);
        when(collegeRepository.existsByCollegeCode("1002")).thenReturn(true);

        ImportSummaryResponse response = collegeImportService.importCsv(file.toString(), false, false, 1024, 5);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(1, response.getRowsProcessed());
        assertEquals(0, response.getRowsInserted());
        assertEquals(1, response.getRowsSkipped());
        assertEquals(1, response.getDuplicateRows());
        verify(collegeRepository, never()).saveAll(anyList());
    }
}
