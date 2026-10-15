package com.focus.adapt.api.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.Badge;
import com.focus.adapt.api.domain.entity.enums.BadgeCategory;

public interface BadgeRepository extends JpaRepository<Badge, UUID> {
    Optional<Badge> findByName(String name);
    Optional<Badge> findByCode(String code);
    boolean existsByCode(String code);
    List<Badge> findByCategory(BadgeCategory category);
    List<Badge> findByCategoryOrderByThresholdAsc(BadgeCategory category);
}
