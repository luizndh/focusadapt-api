package com.focus.adapt.api.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.focus.adapt.api.application.dto.DashboardResponse;
import com.focus.adapt.api.application.dto.LeaderboardEntryResponse;
import com.focus.adapt.api.application.service.DashboardService;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.infrastructure.exception.ApiResponse;
import com.focus.adapt.api.infrastructure.security.CurrentUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard information and gamification stats")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Get current user dashboard and gamification summary statistics")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(@CurrentUser User user) {
        DashboardResponse response = dashboardService.getDashboard(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Get stimulus seeker leaderboard (Top 5)")
    public ResponseEntity<ApiResponse<java.util.List<LeaderboardEntryResponse>>> getLeaderboard(@CurrentUser User user) {
        java.util.List<LeaderboardEntryResponse> response = dashboardService.getStimulusSeekerLeaderboard(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
