package com.campusseekers.mapper;

import com.campusseekers.dto.CutoffImportDto;
import com.campusseekers.entity.Category;
import com.campusseekers.entity.CollegeBranch;
import com.campusseekers.entity.Cutoff;
import com.campusseekers.entity.ExamName;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CutoffImportMapper {

    public Cutoff toEntity(CutoffImportDto dto, CollegeBranch collegeBranch) {
        if (dto == null) {
            return null;
        }

        Cutoff cutoff = new Cutoff();
        cutoff.setCollegeBranch(collegeBranch);

        if (dto.getExamName() != null && !dto.getExamName().isBlank()) {
            try {
                cutoff.setExamName(ExamName.valueOf(dto.getExamName().trim()));
            } catch (IllegalArgumentException e) {
                // Ignore or leave as null
            }
        }

        if (dto.getYear() != null && !dto.getYear().isBlank()) {
            try {
                cutoff.setYear(Integer.parseInt(dto.getYear().trim()));
            } catch (NumberFormatException e) {
                // Ignore or leave as null
            }
        }

        if (dto.getRound() != null && !dto.getRound().isBlank()) {
            try {
                cutoff.setRound(Integer.parseInt(dto.getRound().trim()));
            } catch (NumberFormatException e) {
                // Ignore or leave as null
            }
        }

        if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            try {
                cutoff.setCategory(Category.valueOf(dto.getCategory().trim()));
            } catch (IllegalArgumentException e) {
                // Ignore or leave as null
            }
        }

        cutoff.setRawSeatType(dto.getRawSeatType() != null ? dto.getRawSeatType().trim() : null);

        if (dto.getClosingRank() != null && !dto.getClosingRank().isBlank()) {
            try {
                cutoff.setClosingRank(Integer.parseInt(dto.getClosingRank().trim()));
            } catch (NumberFormatException e) {
                // Ignore or leave as null
            }
        }

        if (dto.getClosingPercentile() != null && !dto.getClosingPercentile().isBlank()) {
            try {
                BigDecimal value = new BigDecimal(dto.getClosingPercentile().trim());
                // Round to exactly 2 decimal places to match precision of NUMERIC(5, 2) in DB
                cutoff.setClosingPercentile(value.setScale(2, RoundingMode.HALF_UP));
            } catch (Exception e) {
                // Ignore or leave as null
            }
        }

        return cutoff;
    }
}
