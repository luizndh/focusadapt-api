package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.StudyMode;
import com.focus.adapt.api.domain.entity.enums.StudySessionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySessionRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Study mode is required")
    private StudyMode studyMode;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @NotNull(message = "Status is required")
    private StudySessionStatus status;

    private String notes;

    @NotEmpty(message = "At least one study task is required")
    private List<UUID> studyTaskIds;

    private UUID studyPlanId;

    // Pomodoro-specific
    private Integer workDurationMinutes;
    private Integer breakDurationMinutes;
    private Integer completedCycles;
    private Integer effectiveStudyMinutes;
}
