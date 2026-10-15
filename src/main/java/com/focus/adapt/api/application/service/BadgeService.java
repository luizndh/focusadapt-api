package com.focus.adapt.api.application.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.focus.adapt.api.application.dto.BadgeResponse;
import com.focus.adapt.api.application.dto.UserAchievementResponse;
import com.focus.adapt.api.application.mapper.BadgeMapper;
import com.focus.adapt.api.domain.entity.Badge;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.UserAchievement;
import com.focus.adapt.api.infrastructure.exception.ResourceNotFoundException;
import com.focus.adapt.api.infrastructure.repository.BadgeRepository;
import com.focus.adapt.api.infrastructure.repository.UserAchievementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final BadgeMapper badgeMapper;

    @Transactional(readOnly = true)
    public Page<BadgeResponse> getBadges(Pageable pageable) {
        return badgeRepository.findAll(pageable).map(badgeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public BadgeResponse getBadgeById(UUID id) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Badge not found with ID: " + id));
        return badgeMapper.toResponse(badge);
    }

    @Transactional(readOnly = true)
    public Page<UserAchievementResponse> getMyAchievements(User user, Pageable pageable) {
        Page<UserAchievement> achievements = userAchievementRepository.findByUser(user, pageable);
        return achievements.map(badgeMapper::toResponse);
    }
}
