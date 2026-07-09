package com.focus.adapt.api.application.mapper;

import com.focus.adapt.api.application.dto.StudySessionRequest;
import com.focus.adapt.api.application.dto.StudySessionResponse;
import com.focus.adapt.api.domain.entity.StudySession;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface StudySessionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "studyPlan", ignore = true)
    @Mapping(target = "pomodoroSession", ignore = true)
    StudySession toEntity(StudySessionRequest request);

    @Mapping(source = "studyPlan.id", target = "studyPlanId")
    @Mapping(source = "pomodoroSession.id", target = "pomodoroSessionId")
    StudySessionResponse toResponse(StudySession entity);
}
