package com.focus.adapt.api.application.service;

import com.focus.adapt.api.application.dto.NotificationResponse;
import com.focus.adapt.api.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    Page<NotificationResponse> getMyNotifications(Boolean unreadOnly, User user, Pageable pageable);
    void markAsRead(UUID id, User user);
    void deleteNotification(UUID id, User user);
}
