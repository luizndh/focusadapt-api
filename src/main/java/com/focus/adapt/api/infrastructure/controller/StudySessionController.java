package com.focus.adapt.api.infrastructure.controller;

import com.focus.adapt.api.application.dto.StudySessionRequest;
import com.focus.adapt.api.application.dto.StudySessionResponse;
import com.focus.adapt.api.application.service.StudySessionService;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudySessionStatus;
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
@RequestMapping("/v1/study-sessions")
@RequiredArgsConstructor
@Tag(name = "Study Sessions", description = "Endpoints for managing study sessions")
@SecurityRequirement(name = "Bearer Authentication")
public class StudySessionController {

    private final StudySessionService studySessionService;

    @PostMapping
    @Operation(summary = "Create a new study session")
    public ResponseEntity<ApiResponse<StudySessionResponse>> create(
            @Valid @RequestBody StudySessionRequest request,
            @CurrentUser User user
    ) {
        StudySessionResponse created = studySessionService.createStudySession(request, user);
        ApiResponse<StudySessionResponse> response = ApiResponse.success(created, "Study session created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get paginated list of current user's study sessions")
    public ResponseEntity<ApiResponse<Page<StudySessionResponse>>> getMySessions(
            @RequestParam(required = false) StudySessionStatus status,
            @CurrentUser User user,
            @PageableDefault(size = 10, sort = "startTime") Pageable pageable
    ) {
        Page<StudySessionResponse> sessions = studySessionService.getMyStudySessions(status, user, pageable);
        ApiResponse<Page<StudySessionResponse>> response = ApiResponse.success(sessions);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get study session details by ID")
    public ResponseEntity<ApiResponse<StudySessionResponse>> getById(
            @PathVariable UUID id,
            @CurrentUser User user
    ) {
        StudySessionResponse session = studySessionService.getStudySessionById(id, user);
        ApiResponse<StudySessionResponse> response = ApiResponse.success(session);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update study session details")
    public ResponseEntity<ApiResponse<StudySessionResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody StudySessionRequest request,
            @CurrentUser User user
    ) {
        StudySessionResponse updated = studySessionService.updateStudySession(id, request, user);
        ApiResponse<StudySessionResponse> response = ApiResponse.success(updated, "Study session updated successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete study session by ID")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @CurrentUser User user
    ) {
        studySessionService.deleteStudySession(id, user);
        ApiResponse<Void> response = ApiResponse.success(null, "Study session deleted successfully");
        return ResponseEntity.ok(response);
    }
}
