package com.focus.adapt.api.application.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.focus.adapt.api.application.dto.SessionGamificationResult;
import com.focus.adapt.api.domain.entity.Badge;
import com.focus.adapt.api.domain.entity.PointsHistory;
import com.focus.adapt.api.domain.entity.StudySession;
import com.focus.adapt.api.domain.entity.StudyTask;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.UserAchievement;
import com.focus.adapt.api.domain.entity.enums.BadgeCategory;
import com.focus.adapt.api.domain.entity.enums.StudyMode;
import com.focus.adapt.api.domain.entity.enums.StudyPlanStatus;
import com.focus.adapt.api.domain.entity.enums.StudySessionStatus;
import com.focus.adapt.api.domain.entity.enums.StudyTaskStatus;
import com.focus.adapt.api.domain.entity.enums.UserMissionStatus;
import com.focus.adapt.api.infrastructure.repository.BadgeRepository;
import com.focus.adapt.api.infrastructure.repository.PointsHistoryRepository;
import com.focus.adapt.api.infrastructure.repository.StudyPlanRepository;
import com.focus.adapt.api.infrastructure.repository.StudySessionRepository;
import com.focus.adapt.api.infrastructure.repository.StudyTaskRepository;
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
    private UserMissionRepository userMissionRepository;

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private StudyPlanRepository studyPlanRepository;

    @Mock
    private StudyTaskRepository studyTaskRepository;

    @Mock
    private StudySessionRepository studySessionRepository;

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
    void shouldAward50PointsForSingleTaskSessionWhenNoBadgesUnlocked() {
        StudyTask task1 = StudyTask.builder().title("Task 1").build();
        task1.setId(UUID.randomUUID());

        StudySession session = StudySession.builder()
                .title("Single Task Session")
                .studyMode(StudyMode.FREE)
                .studyTasks(List.of(task1))
                .status(StudySessionStatus.COMPLETED)
                .build();

        when(userMissionRepository.findByUserAndStatus(user, UserMissionStatus.IN_PROGRESS))
                .thenReturn(Collections.emptyList());
        when(userAchievementRepository.findByUser(user)).thenReturn(Collections.emptyList());
        when(badgeRepository.findAll()).thenReturn(Collections.emptyList());

        SessionGamificationResult result = gamificationService.processSessionCompletion(session, user);

        assertTrue(result.unlockedBadges().isEmpty());
        assertEquals(50, result.sessionXp());
        assertEquals(0, result.badgeBonusXp());
        assertEquals(50, result.totalXp());
        assertEquals(50, user.getPoints());
        assertEquals(50, session.getXpAwarded());
        verify(userRepository, times(1)).save(user);
        verify(pointsHistoryRepository, times(1)).save(any(PointsHistory.class));
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
                .status(StudySessionStatus.COMPLETED)
                .build();

        when(userMissionRepository.findByUserAndStatus(user, UserMissionStatus.IN_PROGRESS))
                .thenReturn(Collections.emptyList());
        when(userAchievementRepository.findByUser(user)).thenReturn(Collections.emptyList());
        when(badgeRepository.findAll()).thenReturn(Collections.emptyList());

        SessionGamificationResult result = gamificationService.processSessionCompletion(session, user);

        assertEquals(100, result.sessionXp());
        assertEquals(0, result.badgeBonusXp());
        assertEquals(100, result.totalXp());
        assertEquals(100, user.getPoints());
        assertEquals(100, session.getXpAwarded());
        verify(userRepository).save(user);
        verify(pointsHistoryRepository).save(any(PointsHistory.class));
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
                .status(StudySessionStatus.COMPLETED)
                .build();

        when(userMissionRepository.findByUserAndStatus(user, UserMissionStatus.IN_PROGRESS))
                .thenReturn(Collections.emptyList());
        when(userAchievementRepository.findByUser(user)).thenReturn(Collections.emptyList());
        when(badgeRepository.findAll()).thenReturn(Collections.emptyList());

        SessionGamificationResult result = gamificationService.processSessionCompletion(session, user);

        // 2 tasks * 50 + 20 pomodoro bonus = 120
        assertEquals(120, result.sessionXp());
        assertEquals(0, result.badgeBonusXp());
        assertEquals(120, result.totalXp());
        assertEquals(120, user.getPoints());
        assertEquals(120, session.getXpAwarded());
    }

    @Test
    void shouldBeIdempotentWhenSessionAlreadyHasXpAwarded() {
        StudySession session = StudySession.builder()
                .title("Already Processed Session")
                .studyMode(StudyMode.FREE)
                .status(StudySessionStatus.COMPLETED)
                .xpAwarded(120)
                .build();

        SessionGamificationResult result = gamificationService.processSessionCompletion(session, user);

        assertTrue(result.unlockedBadges().isEmpty());
        assertEquals(0, result.sessionXp());
        assertEquals(0, result.badgeBonusXp());
        assertEquals(0, result.totalXp());
        assertEquals(0, user.getPoints());
        verify(userRepository, never()).save(any());
        verify(pointsHistoryRepository, never()).save(any());
        verify(userAchievementRepository, never()).save(any());
    }

    @Test
    void shouldEvaluateAndUnlockMultipleBadgesCumulatively() {
        StudyTask task1 = StudyTask.builder().title("Task 1").build();
        task1.setId(UUID.randomUUID());

        StudySession session = StudySession.builder()
                .title("Session unlocking badges")
                .studyMode(StudyMode.FREE)
                .studyTasks(List.of(task1))
                .status(StudySessionStatus.COMPLETED)
                .durationMinutes(60)
                .build();

        Badge badgeTask1 = Badge.builder()
                .code("STUDY_TASK_1")
                .name("Primeiro Passo")
                .category(BadgeCategory.STUDY_TASK)
                .threshold(1)
                .xpReward(20)
                .build();
        badgeTask1.setId(UUID.randomUUID());

        Badge badgeTask10 = Badge.builder()
                .code("STUDY_TASK_10")
                .name("Rumo ao Progresso")
                .category(BadgeCategory.STUDY_TASK)
                .threshold(10)
                .xpReward(50)
                .build();
        badgeTask10.setId(UUID.randomUUID());

        Badge badgeTask50 = Badge.builder()
                .code("STUDY_TASK_50")
                .name("Produtividade Pura")
                .category(BadgeCategory.STUDY_TASK)
                .threshold(50)
                .xpReward(150)
                .build();
        badgeTask50.setId(UUID.randomUUID());

        when(userMissionRepository.findByUserAndStatus(user, UserMissionStatus.IN_PROGRESS))
                .thenReturn(Collections.emptyList());
        when(userAchievementRepository.findByUser(user)).thenReturn(Collections.emptyList());
        when(badgeRepository.findAll()).thenReturn(List.of(badgeTask1, badgeTask10, badgeTask50));

        // User has 10 distinct completed tasks
        when(studyPlanRepository.countByUserAndStatus(user, StudyPlanStatus.COMPLETED)).thenReturn(0L);
        when(studyTaskRepository.countDistinctByUserAndStatus(user, StudyTaskStatus.COMPLETED)).thenReturn(10L);
        when(studySessionRepository.sumDurationMinutesByUserAndStatus(user, StudySessionStatus.COMPLETED)).thenReturn(60L);

        when(userAchievementRepository.existsByUserAndBadge(user, badgeTask1)).thenReturn(false);
        when(userAchievementRepository.existsByUserAndBadge(user, badgeTask10)).thenReturn(false);

        SessionGamificationResult result = gamificationService.processSessionCompletion(session, user);

        assertEquals(2, result.unlockedBadges().size());
        assertTrue(result.unlockedBadges().contains(badgeTask1));
        assertTrue(result.unlockedBadges().contains(badgeTask10));
        assertFalse(result.unlockedBadges().contains(badgeTask50));

        // Session points: 1 task * 50 = 50. Badges points: 20 + 50 = 70. Total: 120 XP.
        assertEquals(50, result.sessionXp());
        assertEquals(70, result.badgeBonusXp());
        assertEquals(120, result.totalXp());
        assertEquals(120, user.getPoints());
        assertEquals(120, session.getXpAwarded());

        verify(userAchievementRepository, times(2)).save(any(UserAchievement.class));
        // 1 session points history + 2 badge points histories = 3
        verify(pointsHistoryRepository, times(3)).save(any(PointsHistory.class));
    }

    @Test
    void shouldNotUnlockAlreadyUnlockedBadges() {
        StudyTask task1 = StudyTask.builder().title("Task 1").build();
        task1.setId(UUID.randomUUID());

        StudySession session = StudySession.builder()
                .title("Session with already unlocked badge")
                .studyMode(StudyMode.FREE)
                .studyTasks(List.of(task1))
                .status(StudySessionStatus.COMPLETED)
                .build();

        Badge badgeTask1 = Badge.builder()
                .code("STUDY_TASK_1")
                .name("Primeiro Passo")
                .category(BadgeCategory.STUDY_TASK)
                .threshold(1)
                .xpReward(20)
                .build();
        UUID badgeId = UUID.randomUUID();
        badgeTask1.setId(badgeId);

        UserAchievement existingAchievement = UserAchievement.builder()
                .user(user)
                .badge(badgeTask1)
                .build();

        when(userMissionRepository.findByUserAndStatus(user, UserMissionStatus.IN_PROGRESS))
                .thenReturn(Collections.emptyList());
        when(userAchievementRepository.findByUser(user)).thenReturn(List.of(existingAchievement));
        when(badgeRepository.findAll()).thenReturn(List.of(badgeTask1));

        SessionGamificationResult result = gamificationService.processSessionCompletion(session, user);

        assertTrue(result.unlockedBadges().isEmpty());
        assertEquals(50, result.sessionXp());
        assertEquals(0, result.badgeBonusXp());
        assertEquals(50, result.totalXp());
        assertEquals(50, user.getPoints());
        verify(userAchievementRepository, never()).save(any());
    }

    @Test
    void shouldUnlockStudyPlanAndStudyTimeBadges() {
        StudySession session = StudySession.builder()
                .title("Session completing time and plan")
                .studyMode(StudyMode.FREE)
                .status(StudySessionStatus.COMPLETED)
                .build();

        Badge planBadge = Badge.builder()
                .code("STUDY_PLAN_1")
                .name("Primeiro Plano")
                .category(BadgeCategory.STUDY_PLAN)
                .threshold(1)
                .xpReward(20)
                .build();
        planBadge.setId(UUID.randomUUID());

        Badge timeBadge = Badge.builder()
                .code("STUDY_TIME_60")
                .name("Hora de Ouro")
                .category(BadgeCategory.STUDY_TIME)
                .threshold(60)
                .xpReward(20)
                .build();
        timeBadge.setId(UUID.randomUUID());

        when(userMissionRepository.findByUserAndStatus(user, UserMissionStatus.IN_PROGRESS))
                .thenReturn(Collections.emptyList());
        when(userAchievementRepository.findByUser(user)).thenReturn(Collections.emptyList());
        when(badgeRepository.findAll()).thenReturn(List.of(planBadge, timeBadge));

        when(studyPlanRepository.countByUserAndStatus(user, StudyPlanStatus.COMPLETED)).thenReturn(1L);
        when(studyTaskRepository.countDistinctByUserAndStatus(user, StudyTaskStatus.COMPLETED)).thenReturn(0L);
        when(studySessionRepository.sumDurationMinutesByUserAndStatus(user, StudySessionStatus.COMPLETED)).thenReturn(75L);

        when(userAchievementRepository.existsByUserAndBadge(user, planBadge)).thenReturn(false);
        when(userAchievementRepository.existsByUserAndBadge(user, timeBadge)).thenReturn(false);

        SessionGamificationResult result = gamificationService.processSessionCompletion(session, user);

        assertEquals(2, result.unlockedBadges().size());
        assertTrue(result.unlockedBadges().contains(planBadge));
        assertTrue(result.unlockedBadges().contains(timeBadge));
        // Session XP: 50. Badges: 20 + 20 = 40. Total = 90 XP.
        assertEquals(50, result.sessionXp());
        assertEquals(40, result.badgeBonusXp());
        assertEquals(90, result.totalXp());
        assertEquals(90, user.getPoints());
    }
}
