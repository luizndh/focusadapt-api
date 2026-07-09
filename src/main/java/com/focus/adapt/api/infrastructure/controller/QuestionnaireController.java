package com.focus.adapt.api.infrastructure.controller;

import com.focus.adapt.api.application.dto.ProcrastinationProfileResponse;
import com.focus.adapt.api.application.dto.QuestionnaireRequest;
import com.focus.adapt.api.application.dto.QuestionnaireResponse;
import com.focus.adapt.api.application.dto.UserAnswerSubmitRequest;
import com.focus.adapt.api.application.service.QuestionnaireService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/questionnaires")
@RequiredArgsConstructor
@Tag(name = "Questionnaires", description = "Endpoints for managing diagnostic questionnaires and evaluating user profiles")
@SecurityRequirement(name = "Bearer Authentication")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    @PostMapping
    @Operation(summary = "Create a new diagnostic questionnaire (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionnaireResponse>> create(
            @Valid @RequestBody QuestionnaireRequest request
    ) {
        QuestionnaireResponse created = questionnaireService.createQuestionnaire(request);
        ApiResponse<QuestionnaireResponse> response = ApiResponse.success(created, "Questionnaire created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get paginated list of questionnaires")
    public ResponseEntity<ApiResponse<Page<QuestionnaireResponse>>> getQuestionnaires(
            @RequestParam(required = false) Boolean activeOnly,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<QuestionnaireResponse> questionnaires = questionnaireService.getQuestionnaires(activeOnly, pageable);
        ApiResponse<Page<QuestionnaireResponse>> response = ApiResponse.success(questionnaires);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get questionnaire by ID (includes nested questions & options)")
    public ResponseEntity<ApiResponse<QuestionnaireResponse>> getById(@PathVariable UUID id) {
        QuestionnaireResponse questionnaire = questionnaireService.getQuestionnaireById(id);
        ApiResponse<QuestionnaireResponse> response = ApiResponse.success(questionnaire);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update questionnaire header details (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionnaireResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody QuestionnaireRequest request
    ) {
        QuestionnaireResponse updated = questionnaireService.updateQuestionnaire(id, request);
        ApiResponse<QuestionnaireResponse> response = ApiResponse.success(updated, "Questionnaire updated successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete questionnaire by ID (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        questionnaireService.deleteQuestionnaire(id);
        ApiResponse<Void> response = ApiResponse.success(null, "Questionnaire deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/answers")
    @Operation(summary = "Submit user answers to calculate and diagnose procrastination profile type")
    public ResponseEntity<ApiResponse<ProcrastinationProfileResponse>> submit(
            @PathVariable UUID id,
            @Valid @RequestBody UserAnswerSubmitRequest request,
            @CurrentUser User user
    ) {
        ProcrastinationProfileResponse profile = questionnaireService.submitAnswers(id, request, user);
        ApiResponse<ProcrastinationProfileResponse> response = ApiResponse.success(profile, "Diagnostic evaluated successfully");
        return ResponseEntity.ok(response);
    }
}
