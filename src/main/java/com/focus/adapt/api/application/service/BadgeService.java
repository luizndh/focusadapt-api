package com.focus.adapt.api.application.service;

import com.focus.adapt.api.application.dto.BadgeRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final BadgeMapper badgeMapper;

    @Transactional
    public BadgeResponse createBadge(BadgeRequest request) {
        Badge badge = badgeMapper.toEntity(request);
        Badge saved = badgeRepository.save(badge);
        return badgeMapper.toResponse(saved);
    }

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

    @Transactional
    public BadgeResponse updateBadge(UUID id, BadgeRequest request) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Badge not found with ID: " + id));

        badge.setName(request.getName());
        badge.setDescription(request.getDescription());
        badge.setIconUrl(request.getIconUrl());

        Badge updated = badgeRepository.save(badge);
        return badgeMapper.toResponse(updated);
    }

    @Transactional
    public void deleteBadge(UUID id) {
        if (!badgeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Badge not found with ID: " + id);
        }
        badgeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<UserAchievementResponse> getMyAchievements(User user, Pageable pageable) {
        Page<UserAchievement> achievements = userAchievementRepository.findByUser(user, pageable);
        return achievements.map(badgeMapper::toResponse);
    }
}
