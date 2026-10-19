package com.focus.adapt.api.application.service;

import java.time.LocalDate;
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
import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile;
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

    @Test
    void shouldCalculateNextTaskMultiplierProgressionForStimulusSeeker() {
        assertEquals(1.50, gamificationService.calculateNextTaskMultiplier(1, ProcrastinationProfile.STIMULUS_SEEKER), 0.001);
        assertEquals(1.75, gamificationService.calculateNextTaskMultiplier(2, ProcrastinationProfile.STIMULUS_SEEKER), 0.001);
        assertEquals(2.00, gamificationService.calculateNextTaskMultiplier(3, ProcrastinationProfile.STIMULUS_SEEKER), 0.001);
        assertEquals(2.25, gamificationService.calculateNextTaskMultiplier(4, ProcrastinationProfile.STIMULUS_SEEKER), 0.001);
        assertEquals(2.50, gamificationService.calculateNextTaskMultiplier(5, ProcrastinationProfile.STIMULUS_SEEKER), 0.001);
        assertEquals(2.75, gamificationService.calculateNextTaskMultiplier(6, ProcrastinationProfile.STIMULUS_SEEKER), 0.001);
    }

    @Test
    void shouldCalculateExpectedTaskXpWithRoundingForFractionalMultipliers() {
        // 1st task (index 0): 1.0x -> 50 XP
        assertEquals(50, gamificationService.calculateTaskXp(0, ProcrastinationProfile.STIMULUS_SEEKER));
        // 2nd task (index 1): 1.50x -> 75 XP
        assertEquals(75, gamificationService.calculateTaskXp(1, ProcrastinationProfile.STIMULUS_SEEKER));
        // 3rd task (index 2): 1.75x -> Math.round(50 * 1.75) = Math.round(87.5) = 88 XP
        assertEquals(88, gamificationService.calculateTaskXp(2, ProcrastinationProfile.STIMULUS_SEEKER));
        // 4th task (index 3): 2.00x -> 100 XP
        assertEquals(100, gamificationService.calculateTaskXp(3, ProcrastinationProfile.STIMULUS_SEEKER));
        // 5th task (index 4): 2.25x -> Math.round(50 * 2.25) = Math.round(112.5) = 113 XP
        assertEquals(113, gamificationService.calculateTaskXp(4, ProcrastinationProfile.STIMULUS_SEEKER));
        // 6th task (index 5): 2.50x -> 125 XP
        assertEquals(125, gamificationService.calculateTaskXp(5, ProcrastinationProfile.STIMULUS_SEEKER));
        // 7th task (index 6): 2.75x -> Math.round(50 * 2.75) = Math.round(137.5) = 138 XP
        assertEquals(138, gamificationService.calculateTaskXp(6, ProcrastinationProfile.STIMULUS_SEEKER));
    }

    @Test
    void shouldAwardOnlyBaseXpWhenStimulusSeekerEndsSessionAfterFirstTask() {
        user.setProcrastinationProfile(ProcrastinationProfile.STIMULUS_SEEKER);
        StudyTask task1 = createTask("Task 1");

        StudySession session = StudySession.builder()
                .title("1 Task Session")
                .studyMode(StudyMode.FREE)
                .studyTasks(List.of(task1))
                .status(StudySessionStatus.COMPLETED)
                .build();

        mockEmptyGamificationDependencies();

        SessionGamificationResult result = gamificationService.processSessionCompletion(session, user);

        assertEquals(50, result.sessionXp());
        assertEquals(50, result.baseSessionXp());
        assertEquals(0, result.multiplierBonusXp());
        assertEquals(1.0, result.appliedMultiplier(), 0.001);
        assertEquals(50, result.totalXp());
        assertEquals(50, user.getPoints());
    }

    @Test
    void shouldApplyProgressiveMultiplierForStimulusSeekerAcross2And3And4Tasks() {
        user.setProcrastinationProfile(ProcrastinationProfile.STIMULUS_SEEKER);
        StudyTask task1 = createTask("Task 1");
        StudyTask task2 = createTask("Task 2");
        StudyTask task3 = createTask("Task 3");
        StudyTask task4 = createTask("Task 4");

        mockEmptyGamificationDependencies();

        // 2 tasks: 50 + 75 (1.5x) = 125 XP (base 100 + bonus 25)
        StudySession session2 = StudySession.builder()
                .title("2 Tasks Session")
                .studyMode(StudyMode.FREE)
                .studyTasks(List.of(task1, task2))
                .status(StudySessionStatus.COMPLETED)
                .build();
        SessionGamificationResult result2 = gamificationService.processSessionCompletion(session2, user);
        assertEquals(125, result2.sessionXp());
        assertEquals(100, result2.baseSessionXp());
        assertEquals(25, result2.multiplierBonusXp());
        assertEquals(1.50, result2.appliedMultiplier(), 0.001);
        assertEquals(125, user.getPoints());

        // 3 tasks: 50 + 75 (1.5x) + 88 (1.75x) = 213 XP (base 150 + bonus 63)
        user.setPoints(0);
        StudySession session3 = StudySession.builder()
                .title("3 Tasks Session")
                .studyMode(StudyMode.FREE)
                .studyTasks(List.of(task1, task2, task3))
                .status(StudySessionStatus.COMPLETED)
                .build();
        SessionGamificationResult result3 = gamificationService.processSessionCompletion(session3, user);
        assertEquals(213, result3.sessionXp());
        assertEquals(150, result3.baseSessionXp());
        assertEquals(63, result3.multiplierBonusXp());
        assertEquals(1.75, result3.appliedMultiplier(), 0.001);
        assertEquals(213, user.getPoints());

        // 4 tasks: 50 + 75 (1.5x) + 88 (1.75x) + 100 (2.0x) = 313 XP (base 200 + bonus 113)
        user.setPoints(0);
        StudySession session4 = StudySession.builder()
                .title("4 Tasks Session")
                .studyMode(StudyMode.FREE)
                .studyTasks(List.of(task1, task2, task3, task4))
                .status(StudySessionStatus.COMPLETED)
                .build();
        SessionGamificationResult result4 = gamificationService.processSessionCompletion(session4, user);
        assertEquals(313, result4.sessionXp());
        assertEquals(200, result4.baseSessionXp());
        assertEquals(113, result4.multiplierBonusXp());
        assertEquals(2.00, result4.appliedMultiplier(), 0.001);
        assertEquals(313, user.getPoints());
    }

    @Test
    void shouldResetMultiplierInNewStudySessionForStimulusSeeker() {
        user.setProcrastinationProfile(ProcrastinationProfile.STIMULUS_SEEKER);
        StudyTask task1 = createTask("Task 1");
        StudyTask task2 = createTask("Task 2");
        StudyTask task3 = createTask("Task 3");
        StudyTask task4 = createTask("Task 4");

        mockEmptyGamificationDependencies();

        // First session: 3 tasks -> 213 XP (reaches 1.75x on 3rd task, would offer 2.0x for 4th)
        StudySession firstSession = StudySession.builder()
                .title("First Session")
                .studyMode(StudyMode.FREE)
                .studyTasks(List.of(task1, task2, task3))
                .status(StudySessionStatus.COMPLETED)
                .build();
        gamificationService.processSessionCompletion(firstSession, user);
        assertEquals(213, user.getPoints());

        // Second session: starts fresh! 1 task in new session -> 50 XP (1.0x), next offered is 1.5x
        StudySession secondSession = StudySession.builder()
                .title("Second Session")
                .studyMode(StudyMode.FREE)
                .studyTasks(List.of(task4))
                .status(StudySessionStatus.COMPLETED)
                .build();
        SessionGamificationResult secondResult = gamificationService.processSessionCompletion(secondSession, user);

        assertEquals(50, secondResult.sessionXp());
        assertEquals(0, secondResult.multiplierBonusXp());
        assertEquals(1.0, secondResult.appliedMultiplier(), 0.001);
        assertEquals(1.50, gamificationService.calculateNextTaskMultiplier(1, user.getProcrastinationProfile()), 0.001);
        assertEquals(263, user.getPoints());
    }

    @Test
    void shouldNeverApplyMultiplierForPerfectionistUserEvenWithMultipleTasks() {
        user.setProcrastinationProfile(ProcrastinationProfile.PERFECTIONIST);
        StudyTask task1 = createTask("Task 1");
        StudyTask task2 = createTask("Task 2");
        StudyTask task3 = createTask("Task 3");

        mockEmptyGamificationDependencies();

        StudySession session = StudySession.builder()
                .title("Perfectionist 3 Tasks Session")
                .studyMode(StudyMode.CONTINUOUS)
                .studyTasks(List.of(task1, task2, task3))
                .status(StudySessionStatus.COMPLETED)
                .build();

        SessionGamificationResult result = gamificationService.processSessionCompletion(session, user);

        // 3 * 50 = 150 XP (no multiplier)
        assertEquals(150, result.sessionXp());
        assertEquals(150, result.baseSessionXp());
        assertEquals(0, result.multiplierBonusXp());
        assertEquals(1.0, result.appliedMultiplier(), 0.001);
        assertEquals(1.0, gamificationService.calculateNextTaskMultiplier(3, ProcrastinationProfile.PERFECTIONIST), 0.001);
        assertEquals(150, user.getPoints());
    }

    @Test
    void shouldDeduplicateRepeatedTasksInSameSessionToPreventDuplicateMultiplierAndXp() {
        user.setProcrastinationProfile(ProcrastinationProfile.STIMULUS_SEEKER);
        StudyTask task1 = createTask("Task 1");
        StudyTask task2 = createTask("Task 2");

        mockEmptyGamificationDependencies();

        // Même task1 repetida 3 vezes + task2: deve contar apenas 2 tasks distintas (50 + 75 = 125 XP)
        StudySession session = StudySession.builder()
                .title("Session with duplicate task references")
                .studyMode(StudyMode.FREE)
                .studyTasks(List.of(task1, task1, task2, task1))
                .status(StudySessionStatus.COMPLETED)
                .build();

        SessionGamificationResult result = gamificationService.processSessionCompletion(session, user);

        assertEquals(125, result.sessionXp());
        assertEquals(100, result.baseSessionXp());
        assertEquals(25, result.multiplierBonusXp());
        assertEquals(1.50, result.appliedMultiplier(), 0.001);
        assertEquals(125, user.getPoints());
    }

    @Test
    void shouldStartStreakAtOneKeepSameDayIncrementConsecutiveDayAndResetAfterGap() {
        user.setProcrastinationProfile(ProcrastinationProfile.STIMULUS_SEEKER);
        user.setStreak(0);
        user.setLastStudyDate(null);

        mockEmptyGamificationDependencies();

        // 1. Primeiro estudo (lastStudyDate == null) -> streak = 1
        StudySession session1 = StudySession.builder()
                .title("Day 1 Session 1")
                .studyMode(StudyMode.CONTINUOUS)
                .studyTasks(List.of(createTask("Task 1")))
                .status(StudySessionStatus.COMPLETED)
                .build();
        gamificationService.processSessionCompletion(session1, user);
        assertEquals(1, user.getStreak());
        assertEquals(LocalDate.now(), user.getLastStudyDate());

        // 2. Segunda sessão no mesmo dia -> streak permanece 1
        StudySession session2 = StudySession.builder()
                .title("Day 1 Session 2")
                .studyMode(StudyMode.CONTINUOUS)
                .studyTasks(List.of(createTask("Task 2")))
                .status(StudySessionStatus.COMPLETED)
                .build();
        gamificationService.processSessionCompletion(session2, user);
        assertEquals(1, user.getStreak());

        // 3. Estudo no dia seguinte (simulando lastStudyDate = ontem e streak = 3) -> streak = 4
        user.setStreak(3);
        user.setLastStudyDate(LocalDate.now().minusDays(1));
        StudySession session3 = StudySession.builder()
                .title("Consecutive Day Session")
                .studyMode(StudyMode.CONTINUOUS)
                .studyTasks(List.of(createTask("Task 3")))
                .status(StudySessionStatus.COMPLETED)
                .build();
        gamificationService.processSessionCompletion(session3, user);
        assertEquals(4, user.getStreak());
        assertEquals(LocalDate.now(), user.getLastStudyDate());

        // 4. Quebra de sequência (lastStudyDate = 3 dias atrás) -> reinicia em 1 ao estudar
        user.setStreak(4);
        user.setLastStudyDate(LocalDate.now().minusDays(3));
        StudySession session4 = StudySession.builder()
                .title("Session After Gap")
                .studyMode(StudyMode.CONTINUOUS)
                .studyTasks(List.of(createTask("Task 4")))
                .status(StudySessionStatus.COMPLETED)
                .build();
        gamificationService.processSessionCompletion(session4, user);
        assertEquals(1, user.getStreak());
        assertEquals(LocalDate.now(), user.getLastStudyDate());
    }

    @Test
    void shouldResolveEffectiveStreakCorrectlyWhenActiveOrExpired() {
        LocalDate today = LocalDate.of(2026, 10, 19);

        User activeToday = User.builder().streak(5).lastStudyDate(today).build();
        assertEquals(5, GamificationService.resolveEffectiveStreak(activeToday, today));

        User activeYesterday = User.builder().streak(5).lastStudyDate(today.minusDays(1)).build();
        assertEquals(5, GamificationService.resolveEffectiveStreak(activeYesterday, today));

        User expiredTwoDaysAgo = User.builder().streak(5).lastStudyDate(today.minusDays(2)).build();
        assertEquals(0, GamificationService.resolveEffectiveStreak(expiredTwoDaysAgo, today));

        User zeroStreak = User.builder().streak(0).lastStudyDate(today).build();
        assertEquals(0, GamificationService.resolveEffectiveStreak(zeroStreak, today));
    }

    private StudyTask createTask(String title) {
        StudyTask task = StudyTask.builder().title(title).build();
        task.setId(UUID.randomUUID());
        return task;
    }

    private void mockEmptyGamificationDependencies() {
        when(userMissionRepository.findByUserAndStatus(user, UserMissionStatus.IN_PROGRESS))
                .thenReturn(Collections.emptyList());
        when(userAchievementRepository.findByUser(user)).thenReturn(Collections.emptyList());
        when(badgeRepository.findAll()).thenReturn(Collections.emptyList());
    }
}


