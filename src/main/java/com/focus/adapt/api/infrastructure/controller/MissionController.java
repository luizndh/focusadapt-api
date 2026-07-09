package com.focus.adapt.api.infrastructure.controller;

import com.focus.adapt.api.application.dto.MissionRequest;
import com.focus.adapt.api.application.dto.MissionResponse;
import com.focus.adapt.api.application.dto.UserMissionResponse;
import com.focus.adapt.api.application.service.MissionService;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.UserMissionStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/missions")
@RequiredArgsConstructor
@Tag(name = "Missions", description = "Endpoints for managing system missions and user quest progress")
@SecurityRequirement(name = "Bearer Authentication")
public class MissionController {

    private final MissionService missionService;

    @PostMapping
    @Operation(summary = "Create a new mission (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MissionResponse>> create(
            @Valid @RequestBody MissionRequest request
    ) {
        MissionResponse created = missionService.createMission(request);
        ApiResponse<MissionResponse> response = ApiResponse.success(created, "Mission created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get paginated list of system missions")
    public ResponseEntity<ApiResponse<Page<MissionResponse>>> getMissions(
            @RequestParam(required = false) Boolean activeOnly,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<MissionResponse> missions = missionService.getMissions(activeOnly, pageable);
        ApiResponse<Page<MissionResponse>> response = ApiResponse.success(missions);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get mission details by ID")
    public ResponseEntity<ApiResponse<MissionResponse>> getById(@PathVariable UUID id) {
        MissionResponse mission = missionService.getMissionById(id);
        ApiResponse<MissionResponse> response = ApiResponse.success(mission);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update mission details (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MissionResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody MissionRequest request
    ) {
        MissionResponse updated = missionService.updateMission(id, request);
        ApiResponse<MissionResponse> response = ApiResponse.success(updated, "Mission updated successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete mission by ID (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        missionService.deleteMission(id);
        ApiResponse<Void> response = ApiResponse.success(null, "Mission deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-progress")
    @Operation(summary = "Get current user's paginated active/completed mission progress details")
    public ResponseEntity<ApiResponse<Page<UserMissionResponse>>> getMyMissions(
            @RequestParam(required = false) UserMissionStatus status,
            @CurrentUser User user,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<UserMissionResponse> progress = missionService.getMyMissions(status, user, pageable);
        ApiResponse<Page<UserMissionResponse>> response = ApiResponse.success(progress);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/my-progress/{userMissionId}/claim")
    @Operation(summary = "Claim reward points for a completed mission")
    public ResponseEntity<ApiResponse<Void>> claimPoints(
            @PathVariable UUID userMissionId,
            @CurrentUser User user
    ) {
        missionService.claimMissionPoints(userMissionId, user);
        ApiResponse<Void> response = ApiResponse.success(null, "Mission points claimed successfully");
        return ResponseEntity.ok(response);
    }
}
