package com.focus.adapt.api.application.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.focus.adapt.api.application.dto.BadgeResponse;
import com.focus.adapt.api.application.dto.DashboardResponse;
import com.focus.adapt.api.application.dto.LeaderboardEntryResponse;
import com.focus.adapt.api.application.dto.UserMissionResponse;
import com.focus.adapt.api.application.mapper.BadgeMapper;
import com.focus.adapt.api.application.mapper.MissionMapper;
import com.focus.adapt.api.domain.entity.StudySession;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile;
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

    public static int calculateLevel(int points) {
        if (points < 100) {
            return 1;
        } else if (points < 250) {
            return 2;
        } else if (points < 500) {
            return 3;
        } else {
            return 4 + (points - 500) / 300;
        }
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getStimulusSeekerLeaderboard(User currentUser) {
        List<User> topUsers = userRepository.findTopByProcrastinationProfileOrderByPointsDesc(
                ProcrastinationProfile.STIMULUS_SEEKER,
                PageRequest.of(0, 5)
        );

        List<LeaderboardEntryResponse> entries = new ArrayList<>();
        for (int i = 0; i < topUsers.size(); i++) {
            User u = topUsers.get(i);
            boolean isCurrent = currentUser != null && currentUser.getId() != null && currentUser.getId().equals(u.getId());
            entries.add(LeaderboardEntryResponse.builder()
                    .rank(i + 1)
                    .userName(u.getName())
                    .level(calculateLevel(u.getPoints()))
                    .xpPoints(u.getPoints())
                    .isCurrentUser(isCurrent)
                    .build());
        }
        return entries;
    }

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

        // 4. Leaderboard (top 5 stimulus seekers by level/points)
        List<LeaderboardEntryResponse> leaderboard = currentUser.getProcrastinationProfile() == ProcrastinationProfile.STIMULUS_SEEKER
                ? getStimulusSeekerLeaderboard(currentUser)
                : Collections.emptyList();

        return DashboardResponse.builder()
                .userName(currentUser.getName())
                .xpPoints(currentUser.getPoints())
                .streak(GamificationService.resolveEffectiveStreak(currentUser))
                .procrastinationProfile(currentUser.getProcrastinationProfile() != null ? currentUser.getProcrastinationProfile().name() : null)
                .activeMissions(activeMissions)
                .unlockedBadges(unlockedBadges)
                .studyStats(DashboardResponse.StudyStats.builder()
                        .totalStudyMinutes(totalStudyMinutes)
                        .sessionsCount(sessionsCount)
                        .pomodoroSessionsCount(pomodoroSessionsCount)
                        .build())
                .leaderboard(leaderboard)
                .build();
    }
}
