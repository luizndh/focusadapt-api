package com.focus.adapt.api.application.dto;

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
    private String badgeName;
    private String badgeDescription;
    private String badgeIconUrl;
    private LocalDateTime unlockedAt;
}
