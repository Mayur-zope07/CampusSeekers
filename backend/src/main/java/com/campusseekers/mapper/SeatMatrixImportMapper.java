package com.campusseekers.mapper;

import com.campusseekers.dto.SeatMatrixImportDto;
import com.campusseekers.entity.CollegeBranch;
import org.springframework.stereotype.Component;

@Component
public class SeatMatrixImportMapper {

    public void updateEntity(SeatMatrixImportDto dto, CollegeBranch collegeBranch) {
        if (dto == null || collegeBranch == null) {
            return;
        }

        if (dto.getIntakeCapacity() != null && !dto.getIntakeCapacity().isBlank()) {
            try {
                collegeBranch.setIntakeCapacity(Integer.parseInt(dto.getIntakeCapacity().trim()));
            } catch (NumberFormatException e) {
                // Ignore or leave as null
            }
        }
    }
}
