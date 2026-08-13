package com.focus.adapt.api.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.focus.adapt.api.application.dto.UserRequest;
import com.focus.adapt.api.application.dto.UserResponse;
import com.focus.adapt.api.domain.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    User toEntity(UserRequest request);

    UserResponse toResponse(User entity);
}
