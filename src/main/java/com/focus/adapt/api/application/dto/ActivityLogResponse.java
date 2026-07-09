package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.ActivityCategory;
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
public class ActivityLogResponse {
    private UUID id;
    private String action;
    private String details;
    private ActivityCategory category;
    private LocalDateTime createdAt;
}
