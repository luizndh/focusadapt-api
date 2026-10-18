package com.focus.adapt.api.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.focus.adapt.api.application.dto.SessionGamificationResult;
import com.focus.adapt.api.domain.entity.Badge;
import com.focus.adapt.api.domain.entity.Mission;
import com.focus.adapt.api.domain.entity.PointsHistory;
import com.focus.adapt.api.domain.entity.StudySession;
import com.focus.adapt.api.domain.entity.StudyTask;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.UserAchievement;
import com.focus.adapt.api.domain.entity.UserMission;
import com.focus.adapt.api.domain.entity.enums.PointsReason;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GamificationService {

    public static final int BASE_TASK_XP = 50;
    public static final int POMODORO_BONUS_XP = 20;

    private final UserRepository userRepository;
    private final PointsHistoryRepository pointsHistoryRepository;
    private final UserMissionRepository userMissionRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final BadgeRepository badgeRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final StudyTaskRepository studyTaskRepository;
    private final StudySessionRepository studySessionRepository;

    /**
     * Calcula o multiplicador oferecido para a PRÓXIMA StudyTask da sessão
     * após {@code completedTasksInSession} tarefas concluídas na mesma StudySession.
     * Regra: multiplicador = 1.5 + 0.25 * (tasksConcluidasNaSessao - 1) para STIMULUS_SEEKER.
     */
    public static double calculateNextTaskMultiplier(int completedTasksInSession, ProcrastinationProfile profile) {
        if (profile != ProcrastinationProfile.STIMULUS_SEEKER || completedTasksInSession < 1) {
            return 1.0;
        }
        return 1.5 + 0.25 * (completedTasksInSession - 1);
    }

    /**
     * Retorna o multiplicador aplicável à tarefa de índice {@code taskIndexZeroBased} (0 = primeira tarefa da sessão).
     * A primeira tarefa (índice 0) nunca recebe multiplicador (1.0x).
     * A partir da segunda tarefa (índice >= 1), recebe o multiplicador ofertado após {@code taskIndexZeroBased} tarefas concluídas.
     */
    public static double getMultiplierForTaskIndex(int taskIndexZeroBased, ProcrastinationProfile profile) {
        if (profile != ProcrastinationProfile.STIMULUS_SEEKER || taskIndexZeroBased <= 0) {
            return 1.0;
        }
        return calculateNextTaskMultiplier(taskIndexZeroBased, profile);
    }

    /**
     * Calcula o XP elegível para a tarefa na posição {@code taskIndexZeroBased} da StudySession.
     */
    public static int calculateTaskXp(int taskIndexZeroBased, ProcrastinationProfile profile) {
        double multiplier = getMultiplierForTaskIndex(taskIndexZeroBased, profile);
        return (int) Math.round(BASE_TASK_XP * multiplier);
    }

    @Transactional
    public SessionGamificationResult processSessionCompletion(StudySession session, User user) {
        if (session.getXpAwarded() > 0) {
            return SessionGamificationResult.empty();
        }

        List<StudyTask> distinctTasks = extractDistinctTasks(session.getStudyTasks());
        int taskCount = !distinctTasks.isEmpty() ? distinctTasks.size() : 1;

        ProcrastinationProfile profile = user != null ? user.getProcrastinationProfile() : null;
        int baseTasksXp = taskCount * BASE_TASK_XP;
        int multipliedTasksXp = 0;
        for (int i = 0; i < taskCount; i++) {
            multipliedTasksXp += calculateTaskXp(i, profile);
        }

        int multiplierBonusXp = multipliedTasksXp - baseTasksXp;
        double appliedMultiplier = getMultiplierForTaskIndex(taskCount - 1, profile);

        int pomodoroBonus = (session.getStudyMode() == StudyMode.POMODORO) ? POMODORO_BONUS_XP : 0;
        int baseSessionXp = baseTasksXp + pomodoroBonus;
        int sessionPoints = multipliedTasksXp + pomodoroBonus;

        user.setPoints(user.getPoints() + sessionPoints);

        LocalDate today = LocalDate.now();
        LocalDate lastStudy = user.getLastStudyDate();

        if (lastStudy == null) {
            user.setStreak(1);
        } else {
            long daysBetween = ChronoUnit.DAYS.between(lastStudy, today);
            if (daysBetween == 1) {
                user.setStreak(user.getStreak() + 1);
            } else if (daysBetween > 1) {
                user.setStreak(1);
            }
        }
        user.setLastStudyDate(today);

        PointsHistory sessionPointsHistory = PointsHistory.builder()
                .user(user)
                .points(sessionPoints)
                .reason(PointsReason.STUDY_SESSION_COMPLETED)
                .description("Completed study session: " + session.getTitle())
                .build();
        pointsHistoryRepository.save(sessionPointsHistory);

        List<UserMission> activeMissions = userMissionRepository.findByUserAndStatus(user, UserMissionStatus.IN_PROGRESS);

        for (UserMission um : activeMissions) {
            Mission m = um.getMission();

            String titleLower = m.getTitle().toLowerCase();
            if (titleLower.contains("session") || titleLower.contains("estudo") || titleLower.contains("foco")) {
                um.setCurrentValue(um.getCurrentValue() + 1);
            } else if ((titleLower.contains("pomodoro") || titleLower.contains("timer")) && session.getStudyMode() == StudyMode.POMODORO) {
                um.setCurrentValue(um.getCurrentValue() + 1);
            } else if (titleLower.contains("streak") || titleLower.contains("sequência") || titleLower.contains("dia")) {
                um.setCurrentValue(user.getStreak());
            } else {
                um.setCurrentValue(um.getCurrentValue() + 1);
            }

            if (um.getCurrentValue() >= m.getTargetValue()) {
                um.setStatus(UserMissionStatus.COMPLETED);
            }
            userMissionRepository.save(um);
        }

        List<Badge> newlyUnlockedBadges = evaluateAndUnlockBadges(user);
        int badgeXpSum = newlyUnlockedBadges.stream().mapToInt(Badge::getXpReward).sum();

        userRepository.save(user);

        int totalAwardedXp = sessionPoints + badgeXpSum;
        session.setXpAwarded(totalAwardedXp);

        return new SessionGamificationResult(
                sessionPoints,
                badgeXpSum,
                totalAwardedXp,
                newlyUnlockedBadges,
                baseSessionXp,
                multiplierBonusXp,
                appliedMultiplier
        );
    }

    private List<StudyTask> extractDistinctTasks(List<StudyTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }
        List<StudyTask> distinct = new ArrayList<>();
        Set<UUID> seenIds = new HashSet<>();
        for (StudyTask task : tasks) {
            if (task == null) {
                continue;
            }
            if (task.getId() != null) {
                if (seenIds.add(task.getId())) {
                    distinct.add(task);
                }
            } else if (!distinct.contains(task)) {
                distinct.add(task);
            }
        }
        return distinct;
    }

    @Transactional
    public List<Badge> evaluateAndUnlockBadges(User user) {
        List<UserAchievement> userAchievements = userAchievementRepository.findByUser(user);
        Set<UUID> unlockedBadgeIds = userAchievements.stream()
                .map(a -> a.getBadge().getId())
                .collect(Collectors.toSet());

        List<Badge> allBadges = badgeRepository.findAll();
        List<Badge> candidateBadges = allBadges.stream()
                .filter(b -> b.getCategory() != null)
                .filter(b -> !unlockedBadgeIds.contains(b.getId()))
                .sorted(Comparator.comparing(Badge::getCategory).thenComparingInt(Badge::getThreshold))
                .toList();

        if (candidateBadges.isEmpty()) {
            return Collections.emptyList();
        }

        long completedPlans = studyPlanRepository.countByUserAndStatus(user, StudyPlanStatus.COMPLETED);
        long completedTasks = studyTaskRepository.countDistinctByUserAndStatus(user, StudyTaskStatus.COMPLETED);
        long totalStudyMinutes = studySessionRepository.sumDurationMinutesByUserAndStatus(user, StudySessionStatus.COMPLETED);

        List<Badge> newlyUnlocked = new ArrayList<>();

        for (Badge badge : candidateBadges) {
            long currentProgress = switch (badge.getCategory()) {
                case STUDY_PLAN -> completedPlans;
                case STUDY_TASK -> completedTasks;
                case STUDY_TIME -> totalStudyMinutes;
            };

            if (currentProgress >= badge.getThreshold()) {
                if (!userAchievementRepository.existsByUserAndBadge(user, badge)) {
                    UserAchievement achievement = UserAchievement.builder()
                            .user(user)
                            .badge(badge)
                            .unlockedAt(LocalDateTime.now())
                            .build();
                    userAchievementRepository.save(achievement);
                    newlyUnlocked.add(badge);

                    if (badge.getXpReward() > 0) {
                        user.setPoints(user.getPoints() + badge.getXpReward());
                        PointsHistory badgePointsHistory = PointsHistory.builder()
                                .user(user)
                                .points(badge.getXpReward())
                                .reason(PointsReason.BADGE_UNLOCKED)
                                .description("Conquista desbloqueada: " + badge.getName())
                                .build();
                        pointsHistoryRepository.save(badgePointsHistory);
                    }
                }
            }
        }

        if (!newlyUnlocked.isEmpty()) {
            userRepository.save(user);
        }

        return newlyUnlocked;
    }
}
