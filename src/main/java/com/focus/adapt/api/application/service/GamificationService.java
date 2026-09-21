package com.focus.adapt.api.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.focus.adapt.api.domain.entity.Badge;
import com.focus.adapt.api.domain.entity.Mission;
import com.focus.adapt.api.domain.entity.Notification;
import com.focus.adapt.api.domain.entity.PointsHistory;
import com.focus.adapt.api.domain.entity.StudySession;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.UserAchievement;
import com.focus.adapt.api.domain.entity.UserMission;
import com.focus.adapt.api.domain.entity.enums.NotificationType;
import com.focus.adapt.api.domain.entity.enums.PointsReason;
import com.focus.adapt.api.domain.entity.enums.StudyMode;
import com.focus.adapt.api.domain.entity.enums.UserMissionStatus;
import com.focus.adapt.api.infrastructure.repository.BadgeRepository;
import com.focus.adapt.api.infrastructure.repository.NotificationRepository;
import com.focus.adapt.api.infrastructure.repository.PointsHistoryRepository;
import com.focus.adapt.api.infrastructure.repository.UserAchievementRepository;
import com.focus.adapt.api.infrastructure.repository.UserMissionRepository;
import com.focus.adapt.api.infrastructure.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final UserRepository userRepository;
    private final PointsHistoryRepository pointsHistoryRepository;
    private final UserMissionRepository userMissionRepository;
    private final NotificationRepository notificationRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final BadgeRepository badgeRepository;

    @Transactional
    public void processSessionCompletion(StudySession session, User user) {
        int taskCount = session.getStudyTasks() != null && !session.getStudyTasks().isEmpty()
                ? session.getStudyTasks().size()
                : 1;
        int pointsAwarded = taskCount * 50;

        // Bonus for Pomodoro sessions
        if (session.getStudyMode() == StudyMode.POMODORO) {
            pointsAwarded += 20;
        }

        user.setPoints(user.getPoints() + pointsAwarded);

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
        userRepository.save(user);

        PointsHistory pointsHistory = PointsHistory.builder()
                .user(user)
                .points(pointsAwarded)
                .reason(PointsReason.STUDY_SESSION_COMPLETED)
                .description("Completed study session: " + session.getTitle())
                .build();
        pointsHistoryRepository.save(pointsHistory);

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

                Notification notification = Notification.builder()
                        .user(user)
                        .title("Mission Completed!")
                        .content("You completed the mission: " + m.getTitle() + "! Go to your dashboard to claim your points.")
                        .type(NotificationType.MISSION_COMPLETED)
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);
            }
            userMissionRepository.save(um);
        }

        checkAndUnlockBadges(user);
    }

    private void checkAndUnlockBadges(User user) {
        if (user.getStreak() >= 3) {
            unlockBadgeIfUnique(user, "Consistency Starter", "Achieve a 3-day study streak");
        }
        if (user.getPoints() >= 500) {
            unlockBadgeIfUnique(user, "XP Collector", "Accumulate more than 500 XP");
        }
    }

    private void unlockBadgeIfUnique(User user, String name, String description) {
        Badge badge = badgeRepository.findByName(name)
                .orElseGet(() -> {
                    Badge b = Badge.builder()
                            .name(name)
                            .description(description)
                            .iconUrl("default-badge-icon-url")
                            .build();
                    return badgeRepository.save(b);
                });

        boolean alreadyUnlocked = userAchievementRepository.existsByUserAndBadge(user, badge);
        if (!alreadyUnlocked) {
            UserAchievement achievement = UserAchievement.builder()
                    .user(user)
                    .badge(badge)
                    .unlockedAt(LocalDateTime.now())
                    .build();
            userAchievementRepository.save(achievement);

            Notification notification = Notification.builder()
                    .user(user)
                    .title("New Badge Unlocked: " + name + "!")
                    .content("Congratulations! You earned a new badge: " + description)
                    .type(NotificationType.SYSTEM)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        }
    }
}
