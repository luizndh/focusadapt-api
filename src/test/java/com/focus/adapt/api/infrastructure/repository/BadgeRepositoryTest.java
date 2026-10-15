package com.focus.adapt.api.infrastructure.repository;

import com.focus.adapt.api.AbstractIntegrationTest;
import com.focus.adapt.api.domain.entity.Badge;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.UserAchievement;
import com.focus.adapt.api.domain.entity.enums.BadgeCategory;
import com.focus.adapt.api.domain.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BadgeRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindSeededBadgesByCodeAndCategory() {
        Optional<Badge> plan1 = badgeRepository.findByCode("STUDY_PLAN_1");
        assertTrue(plan1.isPresent());
        assertEquals("Primeiro Plano", plan1.get().getName());
        assertEquals(BadgeCategory.STUDY_PLAN, plan1.get().getCategory());
        assertEquals(1, plan1.get().getThreshold());
        assertEquals(20, plan1.get().getXpReward());
        assertEquals("/badge-icons/study-plan-1.png", plan1.get().getIconUrl());

        List<Badge> planBadges = badgeRepository.findByCategoryOrderByThresholdAsc(BadgeCategory.STUDY_PLAN);
        assertEquals(4, planBadges.size());
        assertEquals(1, planBadges.get(0).getThreshold());
        assertEquals(3, planBadges.get(1).getThreshold());
        assertEquals(10, planBadges.get(2).getThreshold());
        assertEquals(25, planBadges.get(3).getThreshold());

        List<Badge> taskBadges = badgeRepository.findByCategoryOrderByThresholdAsc(BadgeCategory.STUDY_TASK);
        assertEquals(4, taskBadges.size());
        assertEquals(1, taskBadges.get(0).getThreshold());
        assertEquals(10, taskBadges.get(1).getThreshold());
        assertEquals(50, taskBadges.get(2).getThreshold());
        assertEquals(100, taskBadges.get(3).getThreshold());

        List<Badge> timeBadges = badgeRepository.findByCategoryOrderByThresholdAsc(BadgeCategory.STUDY_TIME);
        assertEquals(4, timeBadges.size());
        assertEquals(60, timeBadges.get(0).getThreshold());
        assertEquals(300, timeBadges.get(1).getThreshold());
        assertEquals(1500, timeBadges.get(2).getThreshold());
        assertEquals(6000, timeBadges.get(3).getThreshold());
    }

    @Test
    void shouldEnforceUniqueConstraintOnBadgeCode() {
        Badge badge1 = Badge.builder()
                .code("CUSTOM_TEST_CODE")
                .name("Custom Test Badge A")
                .description("Description A")
                .category(BadgeCategory.STUDY_TASK)
                .threshold(5)
                .xpReward(30)
                .iconUrl("/api/v1/badges/images/custom-test-a.png")
                .build();
        badgeRepository.save(badge1);
        badgeRepository.flush();

        Badge duplicateCodeBadge = Badge.builder()
                .code("CUSTOM_TEST_CODE")
                .name("Custom Test Badge B")
                .description("Description B")
                .category(BadgeCategory.STUDY_TASK)
                .threshold(5)
                .xpReward(30)
                .iconUrl("/api/v1/badges/images/custom-test-b.png")
                .build();

        badgeRepository.save(duplicateCodeBadge);
        assertThrows(DataIntegrityViolationException.class, () -> badgeRepository.flush());
    }

    @Test
    void shouldEnforceUniqueConstraintOnUserAchievement() {
        User user = User.builder()
                .name("Achievement User")
                .email("unique_achieve_user@example.com")
                .password("secured_password")
                .role(UserRole.USER)
                .build();
        user = userRepository.save(user);

        Badge badge = badgeRepository.findByCode("STUDY_PLAN_1")
                .orElseThrow(() -> new IllegalStateException("Seeded badge STUDY_PLAN_1 not found"));

        UserAchievement achievement1 = UserAchievement.builder()
                .user(user)
                .badge(badge)
                .unlockedAt(LocalDateTime.now())
                .build();
        userAchievementRepository.save(achievement1);
        userAchievementRepository.flush();

        assertTrue(userAchievementRepository.existsByUserAndBadge(user, badge));

        UserAchievement duplicateAchievement = UserAchievement.builder()
                .user(user)
                .badge(badge)
                .unlockedAt(LocalDateTime.now())
                .build();

        userAchievementRepository.save(duplicateAchievement);
        assertThrows(DataIntegrityViolationException.class, () -> userAchievementRepository.flush());
    }
}
