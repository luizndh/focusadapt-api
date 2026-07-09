package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.UserMissionStatus;
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
public class UserMissionResponse {
    private UUID id;
    private UUID missionId;
    private String missionTitle;
    private String missionDescription;
    private int pointsReward;
    private int currentValue;
    private int targetValue;
    private UserMissionStatus status;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
