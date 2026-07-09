package com.focus.adapt.api.application.service;

import com.focus.adapt.api.application.dto.UserRequest;
import com.focus.adapt.api.application.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse getUserById(UUID id);
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse updateUser(UUID id, UserRequest request);
    void deleteUser(UUID id);
}
