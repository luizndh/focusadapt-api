package com.focus.adapt.api.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.focus.adapt.api.application.dto.StudyTaskRequest;
import com.focus.adapt.api.application.dto.StudyTaskResponse;
import com.focus.adapt.api.domain.entity.StudyTask;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {ContentBlockMapper.class})
public interface StudyTaskMapper {

    @Mapping(target = "status", source = "status", defaultValue = "PENDING")
    StudyTask toEntity(StudyTaskRequest request);

    @Mapping(target = "studyPlanId", source = "studyPlan.id")
    @Mapping(target = "blocks", source = "contentBlocks")
    StudyTaskResponse toResponse(StudyTask entity);
}
