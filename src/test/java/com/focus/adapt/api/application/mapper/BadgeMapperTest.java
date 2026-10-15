package com.focus.adapt.api.application.mapper;

import com.focus.adapt.api.application.dto.BadgeRequest;
import com.focus.adapt.api.application.dto.BadgeResponse;
import com.focus.adapt.api.application.dto.UserAchievementResponse;
import com.focus.adapt.api.domain.entity.Badge;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.UserAchievement;
import com.focus.adapt.api.domain.entity.enums.BadgeCategory;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BadgeMapperTest {

    private final BadgeMapper mapper = Mappers.getMapper(BadgeMapper.class);

    @Test
    void shouldMapBadgeToBadgeResponse() {
        UUID id = UUID.randomUUID();
        Badge badge = Badge.builder()
                .code("STUDY_TASK_10")
                .name("Rumo ao Progresso")
                .description("Conclua 10 tarefas")
                .category(BadgeCategory.STUDY_TASK)
                .threshold(10)
                .xpReward(50)
                .iconUrl("/api/v1/badges/images/study-task-10.png")
                .build();
        badge.setId(id);

        BadgeResponse response = mapper.toResponse(badge);

        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals("STUDY_TASK_10", response.getCode());
        assertEquals("Rumo ao Progresso", response.getName());
        assertEquals("Conclua 10 tarefas", response.getDescription());
        assertEquals(BadgeCategory.STUDY_TASK, response.getCategory());
        assertEquals(10, response.getThreshold());
        assertEquals(50, response.getXpReward());
        assertEquals("/api/v1/badges/images/study-task-10.png", response.getIconUrl());
    }

    @Test
    void shouldMapBadgeRequestToBadgeEntity() {
        BadgeRequest request = BadgeRequest.builder()
                .code("STUDY_TIME_60")
                .name("Hora de Ouro")
                .description("Estude 60 minutos")
                .category(BadgeCategory.STUDY_TIME)
                .threshold(60)
                .xpReward(20)
                .iconUrl("/api/v1/badges/images/study-time-60.png")
                .build();

        Badge badge = mapper.toEntity(request);

        assertNotNull(badge);
        assertEquals("STUDY_TIME_60", badge.getCode());
        assertEquals("Hora de Ouro", badge.getName());
        assertEquals("Estude 60 minutos", badge.getDescription());
        assertEquals(BadgeCategory.STUDY_TIME, badge.getCategory());
        assertEquals(60, badge.getThreshold());
        assertEquals(20, badge.getXpReward());
        assertEquals("/api/v1/badges/images/study-time-60.png", badge.getIconUrl());
    }

    @Test
    void shouldMapUserAchievementToUserAchievementResponse() {
        UUID achievementId = UUID.randomUUID();
        UUID badgeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime unlockedAt = LocalDateTime.now();

        User user = User.builder().name("John Doe").build();
        user.setId(userId);

        Badge badge = Badge.builder()
                .code("STUDY_PLAN_25")
                .name("Arquiteto do Foco")
                .description("Conclua 25 planos")
                .category(BadgeCategory.STUDY_PLAN)
                .threshold(25)
                .xpReward(300)
                .iconUrl("/api/v1/badges/images/study-plan-25.png")
                .build();
        badge.setId(badgeId);

        UserAchievement achievement = UserAchievement.builder()
                .user(user)
                .badge(badge)
                .unlockedAt(unlockedAt)
                .build();
        achievement.setId(achievementId);

        UserAchievementResponse response = mapper.toResponse(achievement);

        assertNotNull(response);
        assertEquals(achievementId, response.getId());
        assertEquals(badgeId, response.getBadgeId());
        assertEquals("STUDY_PLAN_25", response.getBadgeCode());
        assertEquals("Arquiteto do Foco", response.getBadgeName());
        assertEquals("Conclua 25 planos", response.getBadgeDescription());
        assertEquals(BadgeCategory.STUDY_PLAN, response.getBadgeCategory());
        assertEquals(25, response.getBadgeThreshold());
        assertEquals(300, response.getBadgeXpReward());
        assertEquals("/api/v1/badges/images/study-plan-25.png", response.getBadgeIconUrl());
        assertEquals(unlockedAt, response.getUnlockedAt());
    }
}
