package com.focus.adapt.api.application.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.focus.adapt.api.application.dto.NotificationResponse;
import com.focus.adapt.api.application.mapper.NotificationMapper;
import com.focus.adapt.api.application.service.NotificationService;
import com.focus.adapt.api.domain.entity.Notification;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.infrastructure.exception.BusinessException;
import com.focus.adapt.api.infrastructure.exception.ResourceNotFoundException;
import com.focus.adapt.api.infrastructure.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Boolean unreadOnly, User user, Pageable pageable) {
        Page<Notification> notifications;
        if (unreadOnly != null && unreadOnly) {
            notifications = notificationRepository.findByUserAndIsRead(user, false, pageable);
        } else {
            notifications = notificationRepository.findByUser(user, pageable);
        }
        return notifications.map(notificationMapper::toResponse);
    }

    @Override
    @Transactional
    public void markAsRead(UUID id, User user) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to modify this notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID id, User user) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to delete this notification");
        }

        notificationRepository.delete(notification);
    }
}
