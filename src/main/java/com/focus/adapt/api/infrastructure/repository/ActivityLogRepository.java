package com.focus.adapt.api.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.ActivityLog;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.ActivityCategory;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    Page<ActivityLog> findByUser(User user, Pageable pageable);
    Page<ActivityLog> findByUserAndCategory(User user, ActivityCategory category, Pageable pageable);
}
