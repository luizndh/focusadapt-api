package com.focus.adapt.api.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.UserAchievement;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    Page<UserAchievement> findByUser(User user, Pageable pageable);
    boolean existsByUserAndBadge(User user, com.focus.adapt.api.domain.entity.Badge badge);
}
