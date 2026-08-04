package com.campusseekers.mapper;

import com.campusseekers.dto.CutoffRequest;
import com.campusseekers.dto.CutoffResponse;
import com.campusseekers.entity.Cutoff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface CutoffMapper {

    @Mapping(source = "collegeBranch.id", target = "collegeBranchId")
    @Mapping(source = "collegeBranch.college.name", target = "collegeName")
    @Mapping(source = "collegeBranch.branch.name", target = "branchName")
    CutoffResponse toResponse(Cutoff cutoff);

    List<CutoffResponse> toResponseList(List<Cutoff> cutoffs);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "collegeBranch", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Cutoff toEntity(CutoffRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "collegeBranch", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCutoffFromRequest(CutoffRequest request, @MappingTarget Cutoff cutoff);
}
