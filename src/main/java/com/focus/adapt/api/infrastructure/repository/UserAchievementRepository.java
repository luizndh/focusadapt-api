package com.focus.adapt.api.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.Badge;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.UserAchievement;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    Page<UserAchievement> findByUser(User user, Pageable pageable);
    List<UserAchievement> findByUser(User user);
    boolean existsByUserAndBadge(User user, Badge badge);
    Optional<UserAchievement> findByUserAndBadge(User user, Badge badge);
}
