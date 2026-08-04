package com.campusseekers.mapper;

import com.campusseekers.dto.CollegeBranchRequest;
import com.campusseekers.dto.CollegeBranchResponse;
import com.campusseekers.entity.CollegeBranch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface CollegeBranchMapper {

    @Mapping(source = "college.id", target = "collegeId")
    @Mapping(source = "college.name", target = "collegeName")
    @Mapping(source = "branch.id", target = "branchId")
    @Mapping(source = "branch.name", target = "branchName")
    @Mapping(source = "branch.branchCode", target = "branchCode")
    CollegeBranchResponse toResponse(CollegeBranch collegeBranch);

    List<CollegeBranchResponse> toResponseList(List<CollegeBranch> collegeBranches);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "college", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "cutoffs", ignore = true)
    @Mapping(target = "shortlists", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CollegeBranch toEntity(CollegeBranchRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "college", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "cutoffs", ignore = true)
    @Mapping(target = "shortlists", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCollegeBranchFromRequest(CollegeBranchRequest request, @MappingTarget CollegeBranch collegeBranch);
}
