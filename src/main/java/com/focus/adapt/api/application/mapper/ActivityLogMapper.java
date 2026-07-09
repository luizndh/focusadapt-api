package com.focus.adapt.api.application.mapper;

import com.focus.adapt.api.application.dto.ActivityLogResponse;
import com.focus.adapt.api.domain.entity.ActivityLog;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ActivityLogMapper {
    ActivityLogResponse toResponse(ActivityLog entity);
}
