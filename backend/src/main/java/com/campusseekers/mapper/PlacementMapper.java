package com.campusseekers.mapper;

import com.campusseekers.dto.PlacementRequest;
import com.campusseekers.dto.PlacementResponse;
import com.campusseekers.entity.Placement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface PlacementMapper {

    @Mapping(source = "college.id", target = "collegeId")
    @Mapping(source = "college.name", target = "collegeName")
    PlacementResponse toResponse(Placement placement);

    List<PlacementResponse> toResponseList(List<Placement> placements);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "college", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Placement toEntity(PlacementRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "college", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updatePlacementFromRequest(PlacementRequest request, @MappingTarget Placement placement);
}
