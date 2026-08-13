package com.focus.adapt.api.infrastructure.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.focus.adapt.api.application.dto.QuestionnaireSubmitRequest;
import com.focus.adapt.api.application.dto.UserOnboardingStatusRequest;
import com.focus.adapt.api.application.dto.UserRequest;
import com.focus.adapt.api.application.dto.UserResponse;
import com.focus.adapt.api.application.service.UserService;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.infrastructure.exception.ApiResponse;
import com.focus.adapt.api.infrastructure.security.CurrentUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for managing users")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse createdUser = userService.createUser(request);
        ApiResponse<UserResponse> response = ApiResponse.success(createdUser, "User created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated user profile info")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(@CurrentUser User currentUser) {
        UserResponse response = userService.getCurrentUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/me/onboarding")
    @Operation(summary = "Update onboarding completion for currently authenticated user")
    public ResponseEntity<ApiResponse<UserResponse>> updateOnboarding(
            @CurrentUser User currentUser,
            @Valid @RequestBody UserOnboardingStatusRequest request
    ) {
        UserResponse updated = userService.updateOnboardingStatus(currentUser.getId(), request.getCompleted());
        return ResponseEntity.ok(ApiResponse.success(updated, "Onboarding status updated successfully"));
    }

    @PatchMapping("/me/questionnaire")
    @Operation(summary = "Submit questionnaire answers and calculate procrastination profile")
    public ResponseEntity<ApiResponse<UserResponse>> submitQuestionnaire(
            @CurrentUser User currentUser,
            @RequestBody QuestionnaireSubmitRequest request
    ) {
        UserResponse updated = userService.submitQuestionnaire(currentUser, request.getAnswers());
        return ResponseEntity.ok(ApiResponse.success(updated, "Questionnaire submitted successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        ApiResponse<UserResponse> response = ApiResponse.success(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all users (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<UserResponse>>> getAllUsers(
            @org.springframework.data.web.PageableDefault(size = 10, sort = "createdAt") org.springframework.data.domain.Pageable pageable
    ) {
        org.springframework.data.domain.Page<UserResponse> users = userService.getAllUsers(pageable);
        ApiResponse<org.springframework.data.domain.Page<UserResponse>> response = ApiResponse.success(users);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user details")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserRequest request) {
        UserResponse updatedUser = userService.updateUser(id, request);
        ApiResponse<UserResponse> response = ApiResponse.success(updatedUser, "User updated successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by ID (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        ApiResponse<Void> response = ApiResponse.success(null, "User deleted successfully");
        return ResponseEntity.ok(response);
    }
}
