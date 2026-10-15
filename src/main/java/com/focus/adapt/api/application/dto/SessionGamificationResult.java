package com.focus.adapt.api.application.dto;

import java.util.Collections;
import java.util.List;

import com.focus.adapt.api.domain.entity.Badge;

public record SessionGamificationResult(
        int sessionXp,
        int badgeBonusXp,
        int totalXp,
        List<Badge> unlockedBadges
) {
    public static SessionGamificationResult empty() {
        return new SessionGamificationResult(0, 0, 0, Collections.emptyList());
    }
}
