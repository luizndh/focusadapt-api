package com.focus.adapt.api.application.mapper;

import com.focus.adapt.api.application.dto.StudyPlanRequest;
import com.focus.adapt.api.application.dto.StudyPlanResponse;
import com.focus.adapt.api.domain.entity.StudyPlan;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface StudyPlanMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "studySessions", ignore = true)
    StudyPlan toEntity(StudyPlanRequest request);

    StudyPlanResponse toResponse(StudyPlan entity);
}
