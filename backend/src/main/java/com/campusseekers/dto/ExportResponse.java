package com.campusseekers.dto;

public record ExportResponse(
        String fileName,
        String contentType,
        byte[] fileContent
) {}
