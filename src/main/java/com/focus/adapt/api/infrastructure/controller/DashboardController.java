package com.focus.adapt.api.infrastructure.controller;

import com.focus.adapt.api.application.dto.*;
import com.focus.adapt.api.application.mapper.*;
import com.focus.adapt.api.infrastructure.exception.ApiResponse;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudyPlanStatus;
import com.focus.adapt.api.domain.entity.enums.UserMissionStatus;
import com.focus.adapt.api.infrastructure.repository.*;
import com.focus.adapt.api.infrastructure.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard information and gamification stats")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final StudyPlanRepository studyPlanRepository;
    private final UserMissionRepository userMissionRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final NotificationRepository notificationRepository;
    private final PomodoroSessionRepository pomodoroSessionRepository;

    private final StudyPlanMapper studyPlanMapper;
    private final MissionMapper missionMapper;
    private final BadgeMapper badgeMapper;
    private final NotificationMapper notificationMapper;
    private final QuestionnaireMapper questionnaireMapper;

    @GetMapping
    @Operation(summary = "Get current user dashboard and gamification summary statistics")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(@CurrentUser User user) {
        // 1. Active study plan
        StudyPlanResponse activePlan = studyPlanRepository.findByUserAndStatus(user, StudyPlanStatus.ACTIVE, PageRequest.of(0, 1))
                .getContent().stream()
                .map(studyPlanMapper::toResponse)
                .findFirst()
                .orElse(null);

        // 2. Active missions
        List<UserMissionResponse> activeMissions = userMissionRepository.findByUserAndStatus(user, UserMissionStatus.IN_PROGRESS)
                .stream()
                .map(missionMapper::toResponse)
                .collect(Collectors.toList());

        // 3. Unlocked badges
        List<BadgeResponse> unlockedBadges = userAchievementRepository.findByUser(user, Pageable.unpaged())
                .getContent().stream()
                .map(ua -> badgeMapper.toResponse(ua.getBadge()))
                .collect(Collectors.toList());

        // 4. Recent notifications
        List<NotificationResponse> recentNotifications = notificationRepository.findByUserAndRead(user, false, PageRequest.of(0, 5))
                .getContent().stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());

        // 5. Pomodoro stats
        List<com.focus.adapt.api.domain.entity.PomodoroSession> pomodoros = pomodoroSessionRepository.findByUser(user, Pageable.unpaged())
                .getContent();
        int totalMinutes = pomodoros.stream()
                .filter(p -> p.getStatus() == com.focus.adapt.api.domain.entity.enums.PomodoroStatus.COMPLETED)
                .mapToInt(com.focus.adapt.api.domain.entity.PomodoroSession::getWorkDurationMinutes)
                .sum();
        int completedSessions = (int) pomodoros.stream()
                .filter(p -> p.getStatus() == com.focus.adapt.api.domain.entity.enums.PomodoroStatus.COMPLETED)
                .count();

        DashboardResponse dashboard = DashboardResponse.builder()
                .userName(user.getName())
                .xpPoints(user.getPoints())
                .streak(user.getStreak())
                .procrastinationProfile(user.getProcrastinationProfile() != null ? questionnaireMapper.toResponse(user.getProcrastinationProfile()) : null)
                .activeStudyPlan(activePlan)
                .activeMissions(activeMissions)
                .unlockedBadges(unlockedBadges)
                .recentNotifications(recentNotifications)
                .pomodoroStats(DashboardResponse.PomodoroStats.builder()
                        .totalWorkMinutes(totalMinutes)
                        .sessionsCount(completedSessions)
                        .build())
                .build();

        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
