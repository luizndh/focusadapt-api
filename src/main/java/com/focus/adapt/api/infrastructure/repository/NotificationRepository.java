package com.focus.adapt.api.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.Notification;
import com.focus.adapt.api.domain.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUser(User user, Pageable pageable);
    Page<Notification> findByUserAndIsRead(User user, boolean isRead, Pageable pageable);
}
