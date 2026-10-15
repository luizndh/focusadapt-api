package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.BadgeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievementResponse {
    private UUID id;
    private UUID badgeId;
    private String badgeCode;
    private String badgeName;
    private String badgeDescription;
    private BadgeCategory badgeCategory;
    private int badgeThreshold;
    private int badgeXpReward;
    private String badgeIconUrl;
    private LocalDateTime unlockedAt;
}
