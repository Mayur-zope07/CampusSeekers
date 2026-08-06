package com.campusseekers.service;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvImportServiceTest {

    private final CsvImportService csvImportService = new CsvImportService();

    @Test
    void parseCsv_ShouldThrowFileNotFound_WhenFileDoesNotExist() {
        assertThrows(FileNotFoundException.class, () ->
                csvImportService.parseCsv("non_existent_file.csv", new String[]{"header1"}, 1024)
        );
    }

    @Test
    void parseCsv_ShouldThrowException_WhenFileSizeExceeded(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "header1,header2\nvalue1,value2");

        assertThrows(IllegalArgumentException.class, () ->
                csvImportService.parseCsv(file.toString(), new String[]{"header1"}, 5)
        );
    }

    @Test
    void parseCsv_ShouldThrowException_WhenHeaderMissing(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "header1,header2\nvalue1,value2");

        assertThrows(IllegalArgumentException.class, () ->
                csvImportService.parseCsv(file.toString(), new String[]{"header3"}, 1024)
        );
    }

    @Test
    void parseCsv_ShouldParseSuccessfully_WhenFileIsValid(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "header1,header2\n\"value, with, comma\",value2");

        try (CSVParser parser = csvImportService.parseCsv(file.toString(), new String[]{"header1", "header2"}, 1024)) {
            assertNotNull(parser);
            List<CSVRecord> records = parser.getRecords();
            assertEquals(1, records.size());
            assertEquals("value, with, comma", records.get(0).get("header1"));
            assertEquals("value2", records.get(0).get("header2"));
        }
    }
}
