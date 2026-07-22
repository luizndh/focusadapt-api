package com.focus.adapt.api.application.mapper;

import com.focus.adapt.api.application.dto.StudyTaskRequest;
import com.focus.adapt.api.application.dto.StudyTaskResponse;
import com.focus.adapt.api.domain.entity.StudyTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudyTaskMapper {

    @Mapping(target = "status", source = "status", defaultValue = "PENDING")
    StudyTask toEntity(StudyTaskRequest request);

    @Mapping(target = "studyPlanId", source = "studyPlan.id")
    StudyTaskResponse toResponse(StudyTask entity);
}
