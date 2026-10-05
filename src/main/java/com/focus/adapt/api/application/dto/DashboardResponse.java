package com.focus.adapt.api.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private String userName;
    private int xpPoints;
    private int streak;
    private String procrastinationProfile;
    private List<UserMissionResponse> activeMissions;
    private List<BadgeResponse> unlockedBadges;
    private StudyStats studyStats;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudyStats {
        private int totalStudyMinutes;
        private int sessionsCount;
        private int pomodoroSessionsCount;
    }
}
