package com.campusseekers.mapper;

import com.campusseekers.dto.BranchRequest;
import com.campusseekers.dto.BranchResponse;
import com.campusseekers.entity.Branch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface BranchMapper {

    BranchResponse toResponse(Branch branch);

    List<BranchResponse> toResponseList(List<Branch> branches);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "collegeBranches", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Branch toEntity(BranchRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "collegeBranches", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateBranchFromRequest(BranchRequest request, @MappingTarget Branch branch);
}
