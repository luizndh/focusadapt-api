package com.focus.adapt.api.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.focus.adapt.api.domain.entity.StudyPlan;
import com.focus.adapt.api.domain.entity.StudyTask;
import com.focus.adapt.api.domain.entity.enums.StudyTaskStatus;

public interface StudyTaskRepository extends JpaRepository<StudyTask, UUID> {
    List<StudyTask> findByStudyPlan(StudyPlan studyPlan);
    Page<StudyTask> findByStudyPlanAndStatus(StudyPlan studyPlan, StudyTaskStatus status, Pageable pageable);
}
