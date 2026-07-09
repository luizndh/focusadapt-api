package com.focus.adapt.api.infrastructure.controller;

import com.focus.adapt.api.application.dto.BadgeRequest;
import com.focus.adapt.api.application.dto.BadgeResponse;
import com.focus.adapt.api.application.dto.UserAchievementResponse;
import com.focus.adapt.api.application.service.BadgeService;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.infrastructure.exception.ApiResponse;
import com.focus.adapt.api.infrastructure.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/badges")
@RequiredArgsConstructor
@Tag(name = "Badges", description = "Endpoints for managing system badges and tracking user achievements")
@SecurityRequirement(name = "Bearer Authentication")
public class BadgeController {

    private final BadgeService badgeService;

    @PostMapping
    @Operation(summary = "Create a new badge (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BadgeResponse>> create(
            @Valid @RequestBody BadgeRequest request
    ) {
        BadgeResponse created = badgeService.createBadge(request);
        ApiResponse<BadgeResponse> response = ApiResponse.success(created, "Badge created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get paginated list of all system badges")
    public ResponseEntity<ApiResponse<Page<BadgeResponse>>> getBadges(
            @PageableDefault(size = 10, sort = "name") Pageable pageable
    ) {
        Page<BadgeResponse> badges = badgeService.getBadges(pageable);
        ApiResponse<Page<BadgeResponse>> response = ApiResponse.success(badges);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get badge details by ID")
    public ResponseEntity<ApiResponse<BadgeResponse>> getById(@PathVariable UUID id) {
        BadgeResponse badge = badgeService.getBadgeById(id);
        ApiResponse<BadgeResponse> response = ApiResponse.success(badge);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update badge details (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BadgeResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody BadgeRequest request
    ) {
        BadgeResponse updated = badgeService.updateBadge(id, request);
        ApiResponse<BadgeResponse> response = ApiResponse.success(updated, "Badge updated successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete badge by ID (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        badgeService.deleteBadge(id);
        ApiResponse<Void> response = ApiResponse.success(null, "Badge deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-achievements")
    @Operation(summary = "Get paginated list of current user's unlocked badges")
    public ResponseEntity<ApiResponse<Page<UserAchievementResponse>>> getMyAchievements(
            @CurrentUser User user,
            @PageableDefault(size = 10, sort = "unlockedAt") Pageable pageable
    ) {
        Page<UserAchievementResponse> achievements = badgeService.getMyAchievements(user, pageable);
        ApiResponse<Page<UserAchievementResponse>> response = ApiResponse.success(achievements);
        return ResponseEntity.ok(response);
    }
}
