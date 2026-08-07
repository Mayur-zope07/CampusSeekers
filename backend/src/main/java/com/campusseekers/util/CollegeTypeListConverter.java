package com.campusseekers.util;

import com.campusseekers.entity.CollegeType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class CollegeTypeListConverter implements AttributeConverter<List<CollegeType>, String> {
    private static final String SPLIT_CHAR = ",";

    @Override
    public String convertToDatabaseColumn(List<CollegeType> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return attribute.stream()
                .map(Enum::name)
                .collect(Collectors.joining(SPLIT_CHAR));
    }

    @Override
    public List<CollegeType> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(dbData.split(SPLIT_CHAR))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(CollegeType::valueOf)
                .collect(Collectors.toList());
    }
}
