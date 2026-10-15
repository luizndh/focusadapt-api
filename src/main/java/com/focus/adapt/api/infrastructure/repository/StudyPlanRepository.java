package com.focus.adapt.api.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.StudyPlan;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudyPlanStatus;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, UUID> {
    Page<StudyPlan> findByUser(User user, Pageable pageable);
    Page<StudyPlan> findByUserAndStatus(User user, StudyPlanStatus status, Pageable pageable);
    long countByUserAndStatus(User user, StudyPlanStatus status);
}
