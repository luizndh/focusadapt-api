package com.focus.adapt.api.infrastructure.controller;

import com.focus.adapt.api.application.dto.NotificationResponse;
import com.focus.adapt.api.application.service.NotificationService;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.infrastructure.exception.ApiResponse;
import com.focus.adapt.api.infrastructure.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for user notifications")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get paginated list of current user's notifications")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            @RequestParam(required = false) Boolean unreadOnly,
            @CurrentUser User user,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<NotificationResponse> notifications = notificationService.getMyNotifications(unreadOnly, user, pageable);
        ApiResponse<Page<NotificationResponse>> response = ApiResponse.success(notifications);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable UUID id,
            @CurrentUser User user
    ) {
        notificationService.markAsRead(id, user);
        ApiResponse<Void> response = ApiResponse.success(null, "Notification marked as read successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @CurrentUser User user
    ) {
        notificationService.deleteNotification(id, user);
        ApiResponse<Void> response = ApiResponse.success(null, "Notification deleted successfully");
        return ResponseEntity.ok(response);
    }
}
