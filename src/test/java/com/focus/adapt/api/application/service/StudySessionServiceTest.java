package com.focus.adapt.api.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.focus.adapt.api.application.dto.StudySessionRequest;
import com.focus.adapt.api.application.dto.StudySessionResponse;
import com.focus.adapt.api.application.mapper.StudySessionMapper;
import com.focus.adapt.api.application.service.impl.StudySessionServiceImpl;
import com.focus.adapt.api.domain.entity.ActivityLog;
import com.focus.adapt.api.domain.entity.Badge;
import com.focus.adapt.api.domain.entity.Mission;
import com.focus.adapt.api.domain.entity.Notification;
import com.focus.adapt.api.domain.entity.PointsHistory;
import com.focus.adapt.api.domain.entity.PomodoroSession;
import com.focus.adapt.api.domain.entity.StudySession;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.UserAchievement;
import com.focus.adapt.api.domain.entity.UserMission;
import com.focus.adapt.api.domain.entity.enums.PomodoroStatus;
import com.focus.adapt.api.domain.entity.enums.StudySessionStatus;
import com.focus.adapt.api.domain.entity.enums.UserMissionStatus;
import com.focus.adapt.api.infrastructure.repository.ActivityLogRepository;
import com.focus.adapt.api.infrastructure.repository.BadgeRepository;
import com.focus.adapt.api.infrastructure.repository.NotificationRepository;
import com.focus.adapt.api.infrastructure.repository.PointsHistoryRepository;
import com.focus.adapt.api.infrastructure.repository.PomodoroSessionRepository;
import com.focus.adapt.api.infrastructure.repository.StudySessionRepository;
import com.focus.adapt.api.infrastructure.repository.UserAchievementRepository;
import com.focus.adapt.api.infrastructure.repository.UserMissionRepository;
import com.focus.adapt.api.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class StudySessionServiceTest {

    @Mock
    private StudySessionRepository studySessionRepository;
        @Mock
        private PomodoroSessionRepository pomodoroSessionRepository;
    @Mock
    private StudySessionMapper studySessionMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PointsHistoryRepository pointsHistoryRepository;
    @Mock
    private ActivityLogRepository activityLogRepository;
    @Mock
    private UserMissionRepository userMissionRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserAchievementRepository userAchievementRepository;
    @Mock
    private BadgeRepository badgeRepository;

    @InjectMocks
    private StudySessionServiceImpl studySessionService;

    @Test
    void shouldCompleteStudySessionAndProcessGamification() {
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .points(100)
                .streak(2)
                .lastStudyDate(LocalDate.now().minusDays(1)) // Studied yesterday, should increment streak to 3
                .build();
        user.setId(userId);

        StudySession session = StudySession.builder()
                .title("Database Design")
                .status(StudySessionStatus.SCHEDULED)
                .user(user)
                .pomodoroSession(PomodoroSession.builder()
                        .workDurationMinutes(25)
                        .status(PomodoroStatus.RUNNING)
                        .build())
                .build();

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        StudySessionRequest request = StudySessionRequest.builder()
                .title("Database Design")
                .status(StudySessionStatus.COMPLETED)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .build();

        when(studySessionRepository.save(any(StudySession.class))).thenReturn(session);
        when(studySessionMapper.toResponse(any(StudySession.class))).thenReturn(new StudySessionResponse());

        // Mock Mission verification
        Mission mission = Mission.builder()
                .title("Complete study session")
                .targetValue(1)
                .build();
        UserMission userMission = UserMission.builder()
                .mission(mission)
                .currentValue(0)
                .status(UserMissionStatus.IN_PROGRESS)
                .build();
        when(userMissionRepository.findByUserAndStatus(user, UserMissionStatus.IN_PROGRESS))
                .thenReturn(List.of(userMission));

        // Mock Badge verification
        Badge badge = Badge.builder().name("Consistency Starter").build();
        when(badgeRepository.findByName("Consistency Starter")).thenReturn(Optional.of(badge));
        when(userAchievementRepository.existsByUserAndBadge(user, badge)).thenReturn(false);

        // Execute update
        studySessionService.updateStudySession(sessionId, request, user);

        // Verify Points allocation (50 base + 20 pomodoro = 70 points awarded)
        assertEquals(170, user.getPoints(), "Points should be increased by 70 XP");

        // Verify Streak incremented
        assertEquals(3, user.getStreak(), "Streak should be incremented to 3");

        // Verify Streak date updated
        assertEquals(LocalDate.now(), user.getLastStudyDate());

        // Verify Repository saves
        verify(userRepository, atLeastOnce()).save(user);
        verify(pointsHistoryRepository, times(1)).save(any(PointsHistory.class));
        verify(activityLogRepository, times(1)).save(any(ActivityLog.class));
        verify(userMissionRepository, atLeastOnce()).save(userMission);
        verify(userAchievementRepository, times(1)).save(any(UserAchievement.class));
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }
}
