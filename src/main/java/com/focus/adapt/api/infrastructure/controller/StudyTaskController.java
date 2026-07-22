package com.focus.adapt.api.infrastructure.controller;

import com.focus.adapt.api.application.dto.StudyTaskRequest;
import com.focus.adapt.api.application.dto.StudyTaskResponse;
import com.focus.adapt.api.application.service.StudyTaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/study-tasks")
@RequiredArgsConstructor
@Tag(name = "Study Task", description = "Endpoints for managing study tasks")
public class StudyTaskController {

    private final StudyTaskService studyTaskService;

    @PostMapping
    @Operation(summary = "Create a new study task")
    public ResponseEntity<StudyTaskResponse> createTask(@Valid @RequestBody StudyTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studyTaskService.createTask(request));
    }

    @GetMapping("/plan/{planId}")
    @Operation(summary = "Get tasks by study plan ID")
    public ResponseEntity<List<StudyTaskResponse>> getTasksByPlan(@PathVariable UUID planId) {
        return ResponseEntity.ok(studyTaskService.getTasksByPlan(planId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a study task")
    public ResponseEntity<StudyTaskResponse> updateTask(@PathVariable UUID id, @Valid @RequestBody StudyTaskRequest request) {
        return ResponseEntity.ok(studyTaskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a study task")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        studyTaskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
