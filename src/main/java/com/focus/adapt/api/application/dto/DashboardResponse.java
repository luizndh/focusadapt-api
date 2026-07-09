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
    private ProcrastinationProfileResponse procrastinationProfile;
    private StudyPlanResponse activeStudyPlan;
    private List<UserMissionResponse> activeMissions;
    private List<BadgeResponse> unlockedBadges;
    private List<NotificationResponse> recentNotifications;
    private PomodoroStats pomodoroStats;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PomodoroStats {
        private int totalWorkMinutes;
        private int sessionsCount;
    }
}
