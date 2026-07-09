package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private UUID id;
    private String title;
    private String content;
    private boolean read;
    private NotificationType type;
    private LocalDateTime createdAt;
}
