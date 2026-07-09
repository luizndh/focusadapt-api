package com.focus.adapt.api.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.StudySession;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudySessionStatus;

public interface StudySessionRepository extends JpaRepository<StudySession, UUID> {
    Page<StudySession> findByUser(User user, Pageable pageable);
    Page<StudySession> findByUserAndStatus(User user, StudySessionStatus status, Pageable pageable);
}
