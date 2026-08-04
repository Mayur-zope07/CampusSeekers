package com.campusseekers.mapper;

import com.campusseekers.dto.ExamScoreRequest;
import com.campusseekers.dto.ExamScoreResponse;
import com.campusseekers.entity.ExamScore;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ExamScoreMapper {

    @Mapping(source = "scoreRank", target = "rank")
    @Mapping(source = "scorePercentile", target = "percentile")
    ExamScoreResponse toResponse(ExamScore score);

    List<ExamScoreResponse> toResponseList(List<ExamScore> scores);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "studentProfile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "rank", target = "scoreRank")
    @Mapping(source = "percentile", target = "scorePercentile")
    ExamScore toEntity(ExamScoreRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "studentProfile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "rank", target = "scoreRank")
    @Mapping(source = "percentile", target = "scorePercentile")
    void updateScoreFromRequest(ExamScoreRequest request, @MappingTarget ExamScore score);
}
