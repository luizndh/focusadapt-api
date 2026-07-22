package com.focus.adapt.api.application.mapper;

import com.focus.adapt.api.application.dto.PomodoroSessionResponse;
import com.focus.adapt.api.domain.entity.PomodoroSession;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PomodoroSessionMapper {
    PomodoroSessionResponse toResponse(PomodoroSession entity);
}
