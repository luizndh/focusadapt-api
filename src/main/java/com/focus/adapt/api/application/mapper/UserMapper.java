package com.focus.adapt.api.application.mapper;

import com.focus.adapt.api.application.dto.UserRequest;
import com.focus.adapt.api.application.dto.UserResponse;
import com.focus.adapt.api.domain.entity.User;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "points", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "procrastinationProfile", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "streak", ignore = true)
    @Mapping(target = "lastStudyDate", ignore = true)
    User toEntity(UserRequest request);

    UserResponse toResponse(User entity);
}
