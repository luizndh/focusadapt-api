package com.focus.adapt.api.application.mapper;

import com.focus.adapt.api.application.dto.NotificationResponse;
import com.focus.adapt.api.domain.entity.Notification;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface NotificationMapper {
    NotificationResponse toResponse(Notification entity);
}
