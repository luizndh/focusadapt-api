package com.focus.adapt.api.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.Badge;

public interface BadgeRepository extends JpaRepository<Badge, UUID> {
    java.util.Optional<Badge> findByName(String name);
}
