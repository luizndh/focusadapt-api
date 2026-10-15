package com.focus.adapt.api.infrastructure.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.focus.adapt.api.application.dto.BadgeResponse;
import com.focus.adapt.api.application.dto.UserAchievementResponse;
import com.focus.adapt.api.application.service.BadgeService;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.infrastructure.exception.ApiResponse;
import com.focus.adapt.api.infrastructure.security.CurrentUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/badges")
@RequiredArgsConstructor
@Tag(name = "Badges", description = "Endpoints for managing system badges and tracking user achievements")
@SecurityRequirement(name = "Bearer Authentication")
public class BadgeController {

    private final BadgeService badgeService;

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
