package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.StudySessionStatus;
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
public class StudySessionResponse {
    private UUID id;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int durationMinutes;
    private StudySessionStatus status;
    private String notes;
    private UUID studyPlanId;
    private UUID pomodoroSessionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
