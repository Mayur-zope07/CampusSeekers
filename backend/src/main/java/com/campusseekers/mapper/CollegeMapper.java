package com.campusseekers.mapper;

import com.campusseekers.dto.CollegeRequest;
import com.campusseekers.dto.CollegeResponse;
import com.campusseekers.dto.CollegeSummaryResponse;
import com.campusseekers.entity.College;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface CollegeMapper {

    CollegeResponse toResponse(College college);

    CollegeSummaryResponse toSummaryResponse(College college);

    List<CollegeResponse> toResponseList(List<College> colleges);

    List<CollegeSummaryResponse> toSummaryResponseList(List<College> colleges);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "collegeBranches", ignore = true)
    @Mapping(target = "placements", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    College toEntity(CollegeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "collegeBranches", ignore = true)
    @Mapping(target = "placements", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCollegeFromRequest(CollegeRequest request, @MappingTarget College college);
}
