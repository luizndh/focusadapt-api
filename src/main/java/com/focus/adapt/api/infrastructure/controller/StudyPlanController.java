package com.focus.adapt.api.infrastructure.controller;

import com.focus.adapt.api.application.dto.StudyPlanRequest;
import com.focus.adapt.api.application.dto.StudyPlanResponse;
import com.focus.adapt.api.application.service.StudyPlanService;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudyPlanStatus;
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
@RequestMapping("/v1/study-plans")
@RequiredArgsConstructor
@Tag(name = "Study Plans", description = "Endpoints for managing study plans")
@SecurityRequirement(name = "Bearer Authentication")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @PostMapping
    @Operation(summary = "Create a new study plan")
    public ResponseEntity<ApiResponse<StudyPlanResponse>> create(
            @Valid @RequestBody StudyPlanRequest request,
            @CurrentUser User user
    ) {
        StudyPlanResponse created = studyPlanService.createStudyPlan(request, user);
        ApiResponse<StudyPlanResponse> response = ApiResponse.success(created, "Study plan created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get paginated list of current user's study plans")
    public ResponseEntity<ApiResponse<Page<StudyPlanResponse>>> getMyPlans(
            @RequestParam(required = false) StudyPlanStatus status,
            @CurrentUser User user,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<StudyPlanResponse> plans = studyPlanService.getMyStudyPlans(status, user, pageable);
        ApiResponse<Page<StudyPlanResponse>> response = ApiResponse.success(plans);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get study plan details by ID")
    public ResponseEntity<ApiResponse<StudyPlanResponse>> getById(
            @PathVariable UUID id,
            @CurrentUser User user
    ) {
        StudyPlanResponse plan = studyPlanService.getStudyPlanById(id, user);
        ApiResponse<StudyPlanResponse> response = ApiResponse.success(plan);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update study plan details")
    public ResponseEntity<ApiResponse<StudyPlanResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody StudyPlanRequest request,
            @CurrentUser User user
    ) {
        StudyPlanResponse updated = studyPlanService.updateStudyPlan(id, request, user);
        ApiResponse<StudyPlanResponse> response = ApiResponse.success(updated, "Study plan updated successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete study plan by ID")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @CurrentUser User user
    ) {
        studyPlanService.deleteStudyPlan(id, user);
        ApiResponse<Void> response = ApiResponse.success(null, "Study plan deleted successfully");
        return ResponseEntity.ok(response);
    }
}
