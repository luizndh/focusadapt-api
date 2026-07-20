package com.focus.adapt.api.application.mapper;

import org.mapstruct.Mapper;

import com.focus.adapt.api.application.dto.UserRequest;
import com.focus.adapt.api.application.dto.UserResponse;
import com.focus.adapt.api.domain.entity.ProcrastinationProfile;
import com.focus.adapt.api.domain.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    default User toEntity(UserRequest request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
    }

    default UserResponse toResponse(User entity) {
        return UserResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .onboardingCompleted(entity.isOnboardingCompleted())
                .questionnaireCompleted(entity.isQuestionnaireCompleted())
                .procrastinationProfile(map(entity.getProcrastinationProfile()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    default String map(ProcrastinationProfile value) {
        if (value == null || value.getProfileType() == null) {
            return null;
        }
        return value.getProfileType().name();
    }
}
