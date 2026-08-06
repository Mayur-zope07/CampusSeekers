package com.campusseekers.mapper;

import com.campusseekers.dto.BranchImportDto;
import com.campusseekers.entity.Branch;
import org.springframework.stereotype.Component;

@Component
public class BranchImportMapper {

    public Branch toEntity(BranchImportDto dto) {
        if (dto == null) {
            return null;
        }

        Branch branch = new Branch();
        branch.setName(dto.getName());
        branch.setBranchCode(dto.getBranchCode());
        return branch;
    }
}
