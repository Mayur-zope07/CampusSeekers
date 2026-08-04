package com.campusseekers.mapper;

import com.campusseekers.dto.StudentProfileRequest;
import com.campusseekers.dto.StudentProfileResponse;
import com.campusseekers.entity.StudentProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface StudentProfileMapper {

    @Mapping(source = "user.id", target = "userId")
    StudentProfileResponse toResponse(StudentProfile profile);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "examScores", ignore = true)
    @Mapping(target = "shortlists", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    StudentProfile toEntity(StudentProfileRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "examScores", ignore = true)
    @Mapping(target = "shortlists", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateProfileFromRequest(StudentProfileRequest request, @MappingTarget StudentProfile profile);
}
