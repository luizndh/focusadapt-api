package com.focus.adapt.api.infrastructure.controller;

import com.focus.adapt.api.application.dto.ActivityLogResponse;
import com.focus.adapt.api.application.mapper.ActivityLogMapper;
import com.focus.adapt.api.domain.entity.ActivityLog;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.ActivityCategory;
import com.focus.adapt.api.infrastructure.exception.ApiResponse;
import com.focus.adapt.api.infrastructure.security.CurrentUser;
import com.focus.adapt.api.infrastructure.repository.ActivityLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/activity-logs")
@RequiredArgsConstructor
@Tag(name = "Activity Logs", description = "Endpoints for viewing user activity/audit history logs")
@SecurityRequirement(name = "Bearer Authentication")
public class ActivityLogController {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;

    @GetMapping
    @Operation(summary = "Get paginated activity log history for current user")
    public ResponseEntity<ApiResponse<Page<ActivityLogResponse>>> getMyLogs(
            @RequestParam(required = false) ActivityCategory category,
            @CurrentUser User user,
            @PageableDefault(size = 15, sort = "createdAt") Pageable pageable
    ) {
        Page<ActivityLog> logs;
        if (category == null) {
            logs = activityLogRepository.findByUser(user, pageable);
        } else {
            logs = activityLogRepository.findByUserAndCategory(user, category, pageable);
        }
        Page<ActivityLogResponse> responses = logs.map(activityLogMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
