package com.focus.adapt.api.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.UserMission;
import com.focus.adapt.api.domain.entity.enums.UserMissionStatus;

public interface UserMissionRepository extends JpaRepository<UserMission, UUID> {
    Page<UserMission> findByUser(User user, Pageable pageable);
    Page<UserMission> findByUserAndStatus(User user, UserMissionStatus status, Pageable pageable);
    java.util.List<UserMission> findByUserAndStatus(User user, UserMissionStatus status);
}
