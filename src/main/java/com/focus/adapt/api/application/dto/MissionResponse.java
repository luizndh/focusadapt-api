package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.MissionType;
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
public class MissionResponse {
    private UUID id;
    private String title;
    private String description;
    private int pointsReward;
    private MissionType missionType;
    private int targetValue;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
