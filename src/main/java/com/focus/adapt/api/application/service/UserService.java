package com.focus.adapt.api.application.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.focus.adapt.api.application.dto.UserRequest;
import com.focus.adapt.api.application.dto.UserResponse;
import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfileType;

public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse getUserById(UUID id);
    UserResponse getCurrentUser(UUID id);
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse updateUser(UUID id, UserRequest request);
    UserResponse updateOnboardingStatus(UUID id, boolean completed);
    UserResponse updateQuestionnaireStatus(UUID id, boolean completed, ProcrastinationProfileType profile);
    void deleteUser(UUID id);
}
