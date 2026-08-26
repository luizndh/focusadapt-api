package com.focus.adapt.api.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.focus.adapt.api.application.dto.StudyTaskRequest;
import com.focus.adapt.api.application.dto.StudyTaskResponse;
import com.focus.adapt.api.domain.entity.StudyTask;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {ContentBlockMapper.class})
public interface StudyTaskMapper {

    @Mapping(target = "status", source = "status", defaultValue = "PENDING")
    StudyTask toEntity(StudyTaskRequest request);

    @Named("metadataOnly")
    @Mapping(target = "studyPlanId", source = "studyPlan.id")
    @Mapping(target = "blocks", ignore = true)
    @Mapping(target = "content", ignore = true)
    StudyTaskResponse toResponse(StudyTask entity);

    @Named("withBlocks")
    @Mapping(target = "studyPlanId", source = "studyPlan.id")
    @Mapping(target = "blocks", source = "contentBlocks")
    @Mapping(target = "content", ignore = true)
    StudyTaskResponse toResponseWithBlocks(StudyTask entity);
}
