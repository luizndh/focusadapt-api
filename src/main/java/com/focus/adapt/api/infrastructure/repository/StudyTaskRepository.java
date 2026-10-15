package com.focus.adapt.api.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.focus.adapt.api.domain.entity.StudyPlan;
import com.focus.adapt.api.domain.entity.StudyTask;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudyTaskStatus;

public interface StudyTaskRepository extends JpaRepository<StudyTask, UUID> {
    List<StudyTask> findByStudyPlan(StudyPlan studyPlan);
    List<StudyTask> findByStudyPlanOrderBySortOrderAsc(StudyPlan studyPlan);
    Page<StudyTask> findByStudyPlanAndStatus(StudyPlan studyPlan, StudyTaskStatus status, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT t.id) FROM StudyTask t WHERE t.studyPlan.user = :user AND t.status = :status")
    long countDistinctByUserAndStatus(@Param("user") User user, @Param("status") StudyTaskStatus status);
}
