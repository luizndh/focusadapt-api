package com.focus.adapt.api.application.service;

import com.focus.adapt.api.application.dto.BadgeRequest;
import com.focus.adapt.api.application.dto.BadgeResponse;
import com.focus.adapt.api.application.dto.UserAchievementResponse;
import com.focus.adapt.api.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BadgeService {
    BadgeResponse createBadge(BadgeRequest request);
    Page<BadgeResponse> getBadges(Pageable pageable);
    BadgeResponse getBadgeById(UUID id);
    BadgeResponse updateBadge(UUID id, BadgeRequest request);
    void deleteBadge(UUID id);
    Page<UserAchievementResponse> getMyAchievements(User user, Pageable pageable);
}
