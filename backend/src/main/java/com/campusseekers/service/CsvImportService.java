package com.campusseekers.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class CsvImportService {

    public CSVParser parseCsv(String pathStr, String[] expectedHeaders, long maxFileSize) throws IOException {
        Path path = Paths.get(pathStr);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("CSV file not found at path: " + pathStr);
        }
        
        long fileSize = Files.size(path);
        if (fileSize > maxFileSize) {
            throw new IllegalArgumentException("CSV file size (" + fileSize + " bytes) exceeds maximum limit of " + maxFileSize + " bytes");
        }

        Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .setIgnoreHeaderCase(false)
                .build();

        CSVParser parser = null;
        try {
            parser = new CSVParser(reader, format);
            
            // Validate required headers
            List<String> actualHeaders = parser.getHeaderNames();
            for (String expected : expectedHeaders) {
                if (!actualHeaders.contains(expected)) {
                    throw new IllegalArgumentException("Missing required column header: " + expected);
                }
            }
            return parser;
        } catch (Exception e) {
            if (parser != null) {
                parser.close();
            } else {
                reader.close();
            }
            throw e;
        }
    }
}
