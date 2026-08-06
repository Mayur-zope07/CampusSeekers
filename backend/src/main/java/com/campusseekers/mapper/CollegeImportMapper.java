package com.campusseekers.mapper;

import com.campusseekers.dto.CollegeImportDto;
import com.campusseekers.entity.College;
import com.campusseekers.entity.CollegeStatus;
import com.campusseekers.entity.CollegeType;
import org.springframework.stereotype.Component;

@Component
public class CollegeImportMapper {

    public College toEntity(CollegeImportDto dto) {
        if (dto == null) {
            return null;
        }

        College college = new College();
        college.setName(dto.getName());
        college.setCollegeCode(dto.getCollegeCode());
        college.setWebsite(dto.getWebsite());
        college.setNaacGrade(dto.getNaacGrade());
        college.setCampusSize(dto.getCampusSize());
        college.setLogoUrl(dto.getLogoUrl());

        if (dto.getCollegeType() != null && !dto.getCollegeType().isBlank()) {
            try {
                college.setCollegeType(CollegeType.valueOf(dto.getCollegeType().trim()));
            } catch (IllegalArgumentException e) {
                // Ignore or leave as null
            }
        }

        if (dto.getEstablishmentYear() != null && !dto.getEstablishmentYear().isBlank()) {
            try {
                college.setEstablishmentYear(Integer.parseInt(dto.getEstablishmentYear().trim()));
            } catch (NumberFormatException e) {
                // Ignore or leave as null
            }
        }

        if (dto.getCity() != null && !dto.getCity().isBlank()) {
            college.setCity(dto.getCity().trim());
        }

        if (dto.getState() != null && !dto.getState().isBlank()) {
            college.setState(dto.getState().trim());
        }

        if (dto.getNbaAccredited() != null && !dto.getNbaAccredited().isBlank()) {
            college.setNbaAccredited(Boolean.parseBoolean(dto.getNbaAccredited().trim()));
        } else {
            college.setNbaAccredited(false);
        }

        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                college.setStatus(CollegeStatus.valueOf(dto.getStatus().trim()));
            } catch (IllegalArgumentException e) {
                college.setStatus(CollegeStatus.ACTIVE);
            }
        } else {
            college.setStatus(CollegeStatus.ACTIVE);
        }

        return college;
    }
}
