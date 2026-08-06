package com.campusseekers.mapper;

import com.campusseekers.dto.CollegeBranchImportDto;
import com.campusseekers.entity.Branch;
import com.campusseekers.entity.College;
import com.campusseekers.entity.CollegeBranch;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CollegeBranchImportMapper {

    public CollegeBranch toEntity(CollegeBranchImportDto dto, College college, Branch branch) {
        if (dto == null) {
            return null;
        }

        CollegeBranch collegeBranch = new CollegeBranch();
        collegeBranch.setCollege(college);
        collegeBranch.setBranch(branch);

        if (dto.getIntakeCapacity() != null && !dto.getIntakeCapacity().isBlank()) {
            try {
                collegeBranch.setIntakeCapacity(Integer.parseInt(dto.getIntakeCapacity().trim()));
            } catch (NumberFormatException e) {
                // Ignore or leave as null
            }
        }

        if (dto.getFeesPerYear() != null && !dto.getFeesPerYear().isBlank()) {
            try {
                collegeBranch.setFeesPerYear(new BigDecimal(dto.getFeesPerYear().trim()));
            } catch (Exception e) {
                // Ignore or leave as null
            }
        }

        if (dto.getDurationYears() != null && !dto.getDurationYears().isBlank()) {
            try {
                collegeBranch.setDurationYears(Integer.parseInt(dto.getDurationYears().trim()));
            } catch (NumberFormatException e) {
                // Ignore or leave as null
            }
        }

        return collegeBranch;
    }
}
