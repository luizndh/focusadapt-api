package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.StudyMode;
import com.focus.adapt.api.domain.entity.enums.StudySessionStatus;
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
public class StudySessionResponse {
    private UUID id;
    private String title;
    private StudyMode studyMode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int durationMinutes;
    private int effectiveStudyMinutes;
    private StudySessionStatus status;
    private String notes;
    private int workDurationMinutes;
    private int breakDurationMinutes;
    private int completedCycles;
    private int totalPausesCount;
    private UUID studyPlanId;
    private List<UUID> studyTaskIds;
    private int sessionXp;
    private int baseSessionXp;
    private int multiplierBonusXp;
    @Builder.Default
    private double appliedMultiplier = 1.0;
    private int quizBonusXp;
    private int badgeBonusXp;
    private int totalXp;
    private int xpEarned;
    @Builder.Default
    private List<BadgeResponse> unlockedBadges = new java.util.ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
