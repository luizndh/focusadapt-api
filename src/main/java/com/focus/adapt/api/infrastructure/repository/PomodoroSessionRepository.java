package com.focus.adapt.api.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.PomodoroSession;
import com.focus.adapt.api.domain.entity.User;

public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, UUID> {
    Page<PomodoroSession> findByUser(User user, Pageable pageable);
}
