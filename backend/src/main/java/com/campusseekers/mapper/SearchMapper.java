package com.campusseekers.mapper;

import com.campusseekers.dto.CollegeDetailsResponse;
import com.campusseekers.dto.CollegeListResponse;
import com.campusseekers.dto.CutoffSearchResponse;
import com.campusseekers.entity.College;
import com.campusseekers.entity.Cutoff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {CollegeBranchMapper.class, PlacementMapper.class, CutoffMapper.class},
        builder = @org.mapstruct.Builder(disableBuilder = true)
)
public interface SearchMapper {

    @Mapping(source = "naacGrade", target = "naac")
    @Mapping(source = "nbaAccredited", target = "nba")
    @Mapping(source = "logoUrl", target = "logo")
    CollegeListResponse toListResponse(College college);

    List<CollegeListResponse> toListResponseList(List<College> colleges);

    @Mapping(target = "latestCutoffs", ignore = true)
    @Mapping(source = "collegeBranches", target = "branches")
    CollegeDetailsResponse toDetailsResponse(College college);

    @Mapping(source = "collegeBranch.id", target = "collegeBranchId")
    @Mapping(source = "collegeBranch.college.name", target = "collegeName")
    @Mapping(source = "collegeBranch.branch.name", target = "branchName")
    CutoffSearchResponse toCutoffSearchResponse(Cutoff cutoff);

    List<CutoffSearchResponse> toCutoffSearchResponseList(List<Cutoff> cutoffs);
}
