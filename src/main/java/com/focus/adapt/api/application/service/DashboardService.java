package com.focus.adapt.api.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.focus.adapt.api.application.dto.BadgeResponse;
import com.focus.adapt.api.application.dto.DashboardResponse;
import com.focus.adapt.api.application.dto.UserMissionResponse;
import com.focus.adapt.api.application.mapper.BadgeMapper;
import com.focus.adapt.api.application.mapper.MissionMapper;
import com.focus.adapt.api.domain.entity.StudySession;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudyMode;
import com.focus.adapt.api.domain.entity.enums.StudySessionStatus;
import com.focus.adapt.api.domain.entity.enums.UserMissionStatus;
import com.focus.adapt.api.infrastructure.repository.StudySessionRepository;
import com.focus.adapt.api.infrastructure.repository.UserAchievementRepository;
import com.focus.adapt.api.infrastructure.repository.UserMissionRepository;
import com.focus.adapt.api.infrastructure.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final StudySessionRepository studySessionRepository;
    private final UserMissionRepository userMissionRepository;
    private final UserAchievementRepository userAchievementRepository;

    private final MissionMapper missionMapper;
    private final BadgeMapper badgeMapper;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(User user) {
        User currentUser = user.getId() != null ? userRepository.findById(user.getId()).orElse(user) : user;

        // 1. Active missions
        List<UserMissionResponse> activeMissions = userMissionRepository.findByUserAndStatus(currentUser, UserMissionStatus.IN_PROGRESS)
                .stream()
                .map(missionMapper::toResponse)
                .collect(Collectors.toList());

        // 2. Unlocked badges
        List<BadgeResponse> unlockedBadges = userAchievementRepository.findByUser(currentUser, Pageable.unpaged())
                .getContent().stream()
                .map(ua -> badgeMapper.toResponse(ua.getBadge()))
                .collect(Collectors.toList());

        // 3. Study stats from StudySessions
        List<StudySession> completedSessions = studySessionRepository.findByUserAndStatus(currentUser, StudySessionStatus.COMPLETED, Pageable.unpaged())
                .getContent();

        int totalStudyMinutes = completedSessions.stream()
                .mapToInt(StudySession::getDurationMinutes)
                .sum();

        int sessionsCount = completedSessions.size();

        int pomodoroSessionsCount = (int) completedSessions.stream()
                .filter(s -> s.getStudyMode() == StudyMode.POMODORO)
                .count();

        return DashboardResponse.builder()
                .userName(currentUser.getName())
                .xpPoints(currentUser.getPoints())
                .streak(currentUser.getStreak())
                .procrastinationProfile(currentUser.getProcrastinationProfile() != null ? currentUser.getProcrastinationProfile().name() : null)
                .activeMissions(activeMissions)
                .unlockedBadges(unlockedBadges)
                .studyStats(DashboardResponse.StudyStats.builder()
                        .totalStudyMinutes(totalStudyMinutes)
                        .sessionsCount(sessionsCount)
                        .pomodoroSessionsCount(pomodoroSessionsCount)
                        .build())
                .build();
    }
}
