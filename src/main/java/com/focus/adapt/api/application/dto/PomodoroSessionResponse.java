package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.PomodoroStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PomodoroSessionResponse {
    private UUID id;
    private int workDurationMinutes;
    private int breakDurationMinutes;
    private PomodoroStatus status;
    private int completedCycles;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
