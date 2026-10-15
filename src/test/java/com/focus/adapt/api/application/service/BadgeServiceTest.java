package com.focus.adapt.api.application.service;

import com.focus.adapt.api.application.dto.BadgeResponse;
import com.focus.adapt.api.application.dto.UserAchievementResponse;
import com.focus.adapt.api.application.mapper.BadgeMapper;
import com.focus.adapt.api.domain.entity.Badge;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.UserAchievement;
import com.focus.adapt.api.domain.entity.enums.BadgeCategory;
import com.focus.adapt.api.infrastructure.exception.ResourceNotFoundException;
import com.focus.adapt.api.infrastructure.repository.BadgeRepository;
import com.focus.adapt.api.infrastructure.repository.UserAchievementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private BadgeMapper badgeMapper;

    @InjectMocks
    private BadgeService badgeService;

    private Badge testBadge;
    private BadgeResponse testBadgeResponse;
    private User testUser;
    private UserAchievement testAchievement;
    private UserAchievementResponse testAchievementResponse;

    @BeforeEach
    void setUp() {
        UUID badgeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        testBadge = Badge.builder()
                .code("STUDY_PLAN_1")
                .name("Primeiro Plano")
                .description("Conclua seu primeiro plano de estudos.")
                .category(BadgeCategory.STUDY_PLAN)
                .threshold(1)
                .xpReward(20)
                .iconUrl("/badge-icons/study-plan-1.png")
                .build();
        testBadge.setId(badgeId);

        testBadgeResponse = BadgeResponse.builder()
                .id(badgeId)
                .code("STUDY_PLAN_1")
                .name("Primeiro Plano")
                .description("Conclua seu primeiro plano de estudos.")
                .category(BadgeCategory.STUDY_PLAN)
                .threshold(1)
                .xpReward(20)
                .iconUrl("/badge-icons/study-plan-1.png")
                .build();

        testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
        testUser.setId(userId);

        testAchievement = UserAchievement.builder()
                .user(testUser)
                .badge(testBadge)
                .unlockedAt(LocalDateTime.now())
                .build();

        testAchievementResponse = UserAchievementResponse.builder()
                .id(UUID.randomUUID())
                .badgeId(badgeId)
                .badgeCode("STUDY_PLAN_1")
                .badgeName("Primeiro Plano")
                .badgeDescription("Conclua seu primeiro plano de estudos.")
                .badgeCategory(BadgeCategory.STUDY_PLAN)
                .badgeThreshold(1)
                .badgeXpReward(20)
                .badgeIconUrl("/api/v1/badges/images/study-plan-1.png")
                .unlockedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldGetBadgesPaginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Badge> badgePage = new PageImpl<>(List.of(testBadge));

        when(badgeRepository.findAll(pageable)).thenReturn(badgePage);
        when(badgeMapper.toResponse(testBadge)).thenReturn(testBadgeResponse);

        Page<BadgeResponse> result = badgeService.getBadges(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        BadgeResponse first = result.getContent().get(0);
        assertEquals("STUDY_PLAN_1", first.getCode());
        assertEquals(BadgeCategory.STUDY_PLAN, first.getCategory());
        assertEquals(1, first.getThreshold());
        assertEquals(20, first.getXpReward());
    }

    @Test
    void shouldGetBadgeById() {
        when(badgeRepository.findById(testBadge.getId())).thenReturn(Optional.of(testBadge));
        when(badgeMapper.toResponse(testBadge)).thenReturn(testBadgeResponse);

        BadgeResponse result = badgeService.getBadgeById(testBadge.getId());

        assertNotNull(result);
        assertEquals("STUDY_PLAN_1", result.getCode());
        assertEquals("Primeiro Plano", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenBadgeNotFound() {
        UUID randomId = UUID.randomUUID();
        when(badgeRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> badgeService.getBadgeById(randomId));
    }

    @Test
    void shouldGetMyAchievementsPaginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserAchievement> achievementPage = new PageImpl<>(List.of(testAchievement));

        when(userAchievementRepository.findByUser(testUser, pageable)).thenReturn(achievementPage);
        when(badgeMapper.toResponse(testAchievement)).thenReturn(testAchievementResponse);

        Page<UserAchievementResponse> result = badgeService.getMyAchievements(testUser, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        UserAchievementResponse first = result.getContent().get(0);
        assertEquals("STUDY_PLAN_1", first.getBadgeCode());
        assertEquals("Primeiro Plano", first.getBadgeName());
        assertEquals(BadgeCategory.STUDY_PLAN, first.getBadgeCategory());
        assertEquals(20, first.getBadgeXpReward());
    }
}
