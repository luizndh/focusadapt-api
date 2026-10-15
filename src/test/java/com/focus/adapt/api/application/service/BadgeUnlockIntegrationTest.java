package com.focus.adapt.api.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.focus.adapt.api.AbstractIntegrationTest;
import com.focus.adapt.api.application.dto.StudySessionRequest;
import com.focus.adapt.api.application.dto.StudySessionResponse;
import com.focus.adapt.api.domain.entity.Badge;
import com.focus.adapt.api.domain.entity.PointsHistory;
import com.focus.adapt.api.domain.entity.StudyPlan;
import com.focus.adapt.api.domain.entity.StudySession;
import com.focus.adapt.api.domain.entity.StudyTask;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.UserAchievement;
import com.focus.adapt.api.domain.entity.enums.PointsReason;
import com.focus.adapt.api.domain.entity.enums.StudyMode;
import com.focus.adapt.api.domain.entity.enums.StudyPlanStatus;
import com.focus.adapt.api.domain.entity.enums.StudySessionStatus;
import com.focus.adapt.api.domain.entity.enums.StudyTaskStatus;
import com.focus.adapt.api.domain.entity.enums.UserRole;
import com.focus.adapt.api.infrastructure.repository.BadgeRepository;
import com.focus.adapt.api.infrastructure.repository.PointsHistoryRepository;
import com.focus.adapt.api.infrastructure.repository.StudyPlanRepository;
import com.focus.adapt.api.infrastructure.repository.StudySessionRepository;
import com.focus.adapt.api.infrastructure.repository.StudyTaskRepository;
import com.focus.adapt.api.infrastructure.repository.UserAchievementRepository;
import com.focus.adapt.api.infrastructure.repository.UserRepository;

@SpringBootTest
@Transactional
class BadgeUnlockIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private StudySessionService studySessionService;

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyPlanRepository studyPlanRepository;

    @Autowired
    private StudyTaskRepository studyTaskRepository;

    @Autowired
    private StudySessionRepository studySessionRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private PointsHistoryRepository pointsHistoryRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Test
    void shouldEvaluateAndUnlockBadgesDuringStudySessionCompletion() {
        User user = User.builder()
                .name("Integration User")
                .email("badge_unlock_test_" + UUID.randomUUID() + "@example.com")
                .password("hash")
                .role(UserRole.USER)
                .points(0)
                .streak(0)
                .build();
        user = userRepository.save(user);

        StudyPlan plan = StudyPlan.builder()
                .title("Full Plan")
                .description("Test Plan")
                .category("Computer Science")
                .startDate(java.time.LocalDate.now().minusDays(1))
                .endDate(java.time.LocalDate.now().plusDays(10))
                .status(StudyPlanStatus.ACTIVE)
                .user(user)
                .build();
        plan = studyPlanRepository.save(plan);

        // Create 10 distinct tasks
        List<UUID> taskIds = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            StudyTask task = StudyTask.builder()
                    .title("Task " + i)
                    .sortOrder(i)
                    .status(StudyTaskStatus.PENDING)
                    .studyPlan(plan)
                    .build();
            task = studyTaskRepository.save(task);
            taskIds.add(task.getId());
        }

        LocalDateTime start = LocalDateTime.now().minusMinutes(65);
        LocalDateTime end = LocalDateTime.now();

        StudySessionRequest request = StudySessionRequest.builder()
                .title("Big Study Session")
                .studyMode(StudyMode.FREE)
                .status(StudySessionStatus.COMPLETED)
                .startTime(start)
                .endTime(end)
                .studyPlanId(plan.getId())
                .studyTaskIds(taskIds)
                .build();

        StudySessionResponse response = studySessionService.createStudySession(request, user);

        assertNotNull(response);
        assertFalse(response.getUnlockedBadges().isEmpty());

        // 10 tasks unlock STUDY_TASK_1 and STUDY_TASK_10. 65 minutes unlock STUDY_TIME_60.
        List<String> unlockedCodes = response.getUnlockedBadges().stream()
                .map(b -> b.getCode())
                .toList();

        assertTrue(unlockedCodes.contains("STUDY_TASK_1"), "Should unlock STUDY_TASK_1");
        assertTrue(unlockedCodes.contains("STUDY_TASK_10"), "Should unlock STUDY_TASK_10");
        assertTrue(unlockedCodes.contains("STUDY_TIME_60"), "Should unlock STUDY_TIME_60");

        // Points check:
        // Session: 10 tasks * 50 = 500 XP
        // Badges: TASK_1 (20) + TASK_10 (50) + TIME_60 (20) = 90 XP
        // Total expected = 590 XP
        User reloadedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(590, reloadedUser.getPoints());
        assertEquals(500, response.getSessionXp());
        assertEquals(90, response.getBadgeBonusXp());
        assertEquals(590, response.getTotalXp());
        assertEquals(590, response.getXpEarned());

        // Check user achievements in database
        List<UserAchievement> achievements = userAchievementRepository.findByUser(reloadedUser);
        assertEquals(3, achievements.size());

        // Check PointsHistory records
        List<PointsHistory> histories = pointsHistoryRepository.findAll().stream()
                .filter(h -> h.getUser().getId().equals(reloadedUser.getId()))
                .toList();
        // 1 session points history + 3 badge points histories = 4
        assertEquals(4, histories.size());
        assertTrue(histories.stream().anyMatch(h -> h.getReason() == PointsReason.STUDY_SESSION_COMPLETED));
        assertEquals(3, histories.stream().filter(h -> h.getReason() == PointsReason.BADGE_UNLOCKED).count());

        // Idempotency: repeated call does not grant additional XP
        StudySession sessionEntity = studySessionRepository.findById(response.getId()).orElseThrow();
        com.focus.adapt.api.application.dto.SessionGamificationResult secondPass = gamificationService.processSessionCompletion(sessionEntity, reloadedUser);
        assertTrue(secondPass.unlockedBadges().isEmpty());
        assertEquals(0, secondPass.totalXp());
        assertEquals(590, reloadedUser.getPoints());
    }

    private void assertNotNull(Object obj) {
        org.junit.jupiter.api.Assertions.assertNotNull(obj);
    }
}
