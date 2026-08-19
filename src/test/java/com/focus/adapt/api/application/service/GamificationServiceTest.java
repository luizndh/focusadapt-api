package com.focus.adapt.api.application.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.focus.adapt.api.domain.entity.ActivityLog;
import com.focus.adapt.api.domain.entity.PointsHistory;
import com.focus.adapt.api.domain.entity.StudySession;
import com.focus.adapt.api.domain.entity.StudyTask;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudyMode;
import com.focus.adapt.api.domain.entity.enums.UserMissionStatus;
import com.focus.adapt.api.infrastructure.repository.ActivityLogRepository;
import com.focus.adapt.api.infrastructure.repository.BadgeRepository;
import com.focus.adapt.api.infrastructure.repository.NotificationRepository;
import com.focus.adapt.api.infrastructure.repository.PointsHistoryRepository;
import com.focus.adapt.api.infrastructure.repository.UserAchievementRepository;
import com.focus.adapt.api.infrastructure.repository.UserMissionRepository;
import com.focus.adapt.api.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

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
    private GamificationService gamificationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .points(0)
                .streak(0)
                .build();
        user.setId(UUID.randomUUID());
    }

    @Test
    void shouldAward50PointsForSingleTaskSession() {
        StudyTask task1 = StudyTask.builder().title("Task 1").build();
        task1.setId(UUID.randomUUID());

        StudySession session = StudySession.builder()
                .title("Single Task Session")
                .studyMode(StudyMode.FREE)
                .studyTasks(List.of(task1))
                .build();

        when(userMissionRepository.findByUserAndStatus(user, UserMissionStatus.IN_PROGRESS))
                .thenReturn(Collections.emptyList());

        gamificationService.processSessionCompletion(session, user);

        assertEquals(50, user.getPoints());
        verify(userRepository).save(user);
        verify(pointsHistoryRepository).save(any(PointsHistory.class));
        verify(activityLogRepository).save(any(ActivityLog.class));
    }

    @Test
    void shouldAward100PointsForTwoTaskSession() {
        StudyTask task1 = StudyTask.builder().title("Task 1").build();
        task1.setId(UUID.randomUUID());
        StudyTask task2 = StudyTask.builder().title("Task 2").build();
        task2.setId(UUID.randomUUID());

        StudySession session = StudySession.builder()
                .title("Two Tasks Session")
                .studyMode(StudyMode.CONTINUOUS)
                .studyTasks(List.of(task1, task2))
                .build();

        when(userMissionRepository.findByUserAndStatus(user, UserMissionStatus.IN_PROGRESS))
                .thenReturn(Collections.emptyList());

        gamificationService.processSessionCompletion(session, user);

        assertEquals(100, user.getPoints());
        verify(userRepository).save(user);
        verify(pointsHistoryRepository).save(any(PointsHistory.class));
        verify(activityLogRepository).save(any(ActivityLog.class));
    }

    @Test
    void shouldAwardBonusForPomodoroMode() {
        StudyTask task1 = StudyTask.builder().title("Task 1").build();
        task1.setId(UUID.randomUUID());
        StudyTask task2 = StudyTask.builder().title("Task 2").build();
        task2.setId(UUID.randomUUID());

        StudySession session = StudySession.builder()
                .title("Two Tasks Pomodoro Session")
                .studyMode(StudyMode.POMODORO)
                .studyTasks(List.of(task1, task2))
                .build();

        when(userMissionRepository.findByUserAndStatus(user, UserMissionStatus.IN_PROGRESS))
                .thenReturn(Collections.emptyList());

        gamificationService.processSessionCompletion(session, user);

        // 2 tasks * 50 + 20 pomodoro bonus = 120
        assertEquals(120, user.getPoints());
    }
}
