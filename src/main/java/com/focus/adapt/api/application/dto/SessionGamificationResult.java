package com.focus.adapt.api.application.dto;

import java.util.Collections;
import java.util.List;

import com.focus.adapt.api.domain.entity.Badge;

public record SessionGamificationResult(
        int sessionXp,
        int badgeBonusXp,
        int totalXp,
        List<Badge> unlockedBadges,
        int baseSessionXp,
        int multiplierBonusXp,
        double appliedMultiplier
) {
    public SessionGamificationResult(
            int sessionXp,
            int badgeBonusXp,
            int totalXp,
            List<Badge> unlockedBadges
    ) {
        this(sessionXp, badgeBonusXp, totalXp, unlockedBadges, sessionXp, 0, 1.0);
    }

    public static SessionGamificationResult empty() {
        return new SessionGamificationResult(0, 0, 0, Collections.emptyList(), 0, 0, 1.0);
    }
}
