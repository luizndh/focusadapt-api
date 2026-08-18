package com.focus.adapt.api.application.service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.focus.adapt.api.application.dto.StudySessionRequest;
import com.focus.adapt.api.application.dto.StudySessionResponse;
import com.focus.adapt.api.application.mapper.StudySessionMapper;
import com.focus.adapt.api.domain.entity.StudyPlan;
import com.focus.adapt.api.domain.entity.StudySession;
import com.focus.adapt.api.domain.entity.StudyTask;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudyMode;
import com.focus.adapt.api.domain.entity.enums.StudyPlanStatus;
import com.focus.adapt.api.domain.entity.enums.StudySessionStatus;
import com.focus.adapt.api.domain.entity.enums.StudyTaskStatus;
import com.focus.adapt.api.infrastructure.exception.BusinessException;
import com.focus.adapt.api.infrastructure.exception.ResourceNotFoundException;
import com.focus.adapt.api.infrastructure.repository.StudySessionRepository;
import com.focus.adapt.api.infrastructure.repository.StudyTaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final StudyTaskRepository studyTaskRepository;
    private final StudySessionMapper studySessionMapper;
    private final GamificationService gamificationService;

    @Transactional
    public StudySessionResponse createStudySession(StudySessionRequest request, User user) {
        StudySession session = studySessionMapper.toEntity(request);
        session.setUser(user);

        if (request.getEndTime() != null) {
            long duration = ChronoUnit.MINUTES.between(request.getStartTime(), request.getEndTime());
            session.setDurationMinutes((int) duration);
        }

        if (request.getStudyMode() == StudyMode.POMODORO) {
            session.setWorkDurationMinutes(request.getWorkDurationMinutes() != null ? request.getWorkDurationMinutes() : 25);
            session.setBreakDurationMinutes(request.getBreakDurationMinutes() != null ? request.getBreakDurationMinutes() : 5);
        }

        associateTasksAndPlan(session, request.getStudyTaskIds(), request.getStudyPlanId(), user);

        StudySession saved = studySessionRepository.save(session);

        if (saved.getStatus() == StudySessionStatus.COMPLETED) {
            for (StudyTask task : saved.getStudyTasks()) {
                if (task.getStatus() != StudyTaskStatus.COMPLETED) {
                    task.setStatus(StudyTaskStatus.COMPLETED);
                    studyTaskRepository.save(task);
                }
            }
            gamificationService.processSessionCompletion(saved, user);
        }

        return studySessionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<StudySessionResponse> getMyStudySessions(StudySessionStatus status, User user, Pageable pageable) {
        Page<StudySession> sessions;
        if (status == null) {
            sessions = studySessionRepository.findByUser(user, pageable);
        } else {
            sessions = studySessionRepository.findByUserAndStatus(user, status, pageable);
        }
        return sessions.map(studySessionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public StudySessionResponse getStudySessionById(UUID id, User user) {
        StudySession session = studySessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Study session not found: " + id));
        if (!session.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to view this study session");
        }
        return studySessionMapper.toResponse(session);
    }

    @Transactional
    public StudySessionResponse updateStudySession(UUID id, StudySessionRequest request, User user) {
        StudySession session = studySessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Study session not found: " + id));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to modify this study session");
        }

        StudySessionStatus oldStatus = session.getStatus();

        session.setTitle(request.getTitle());
        session.setStudyMode(request.getStudyMode());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setStatus(request.getStatus());
        session.setNotes(request.getNotes());

        if (request.getEndTime() != null) {
            long duration = ChronoUnit.MINUTES.between(request.getStartTime(), request.getEndTime());
            session.setDurationMinutes((int) duration);
        }

        if (request.getCompletedCycles() != null) {
            session.setCompletedCycles(request.getCompletedCycles());
        }
        if (request.getEffectiveStudyMinutes() != null) {
            session.setEffectiveStudyMinutes(request.getEffectiveStudyMinutes());
        }
        if (request.getWorkDurationMinutes() != null) {
            session.setWorkDurationMinutes(request.getWorkDurationMinutes());
        }
        if (request.getBreakDurationMinutes() != null) {
            session.setBreakDurationMinutes(request.getBreakDurationMinutes());
        }

        associateTasksAndPlan(session, request.getStudyTaskIds(), request.getStudyPlanId(), user);

        StudySession updated = studySessionRepository.save(session);

        if (request.getStatus() == StudySessionStatus.COMPLETED) {
            for (StudyTask task : updated.getStudyTasks()) {
                if (task.getStatus() != StudyTaskStatus.COMPLETED) {
                    task.setStatus(StudyTaskStatus.COMPLETED);
                    studyTaskRepository.save(task);
                }
            }
            if (oldStatus != StudySessionStatus.COMPLETED) {
                gamificationService.processSessionCompletion(updated, user);
            }
        }

        return studySessionMapper.toResponse(updated);
    }

    @Transactional
    public void deleteStudySession(UUID id, User user) {
        StudySession session = studySessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Study session not found: " + id));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to delete this study session");
        }

        studySessionRepository.delete(session);
    }

    private void associateTasksAndPlan(StudySession session, List<UUID> taskIds, UUID requestedPlanId, User user) {
        if (taskIds == null || taskIds.isEmpty()) {
            throw new BusinessException("At least one study task is required");
        }

        List<StudyTask> tasks = new ArrayList<>();
        StudyPlan commonPlan = null;

        for (UUID taskId : taskIds) {
            StudyTask task = studyTaskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Study task not found: " + taskId));

            if (!task.getStudyPlan().getUser().getId().equals(user.getId())) {
                throw new BusinessException("You do not have permission to link to study task: " + taskId);
            }

            ensureActivePlan(task.getStudyPlan());

            if (commonPlan == null) {
                commonPlan = task.getStudyPlan();
            } else if (!commonPlan.getId().equals(task.getStudyPlan().getId())) {
                throw new BusinessException("All study tasks in a session must belong to the same study plan");
            }

            tasks.add(task);
        }

        if (requestedPlanId != null && commonPlan != null && !commonPlan.getId().equals(requestedPlanId)) {
            throw new BusinessException("Study tasks do not belong to the provided study plan");
        }

        session.setStudyTasks(tasks);
        session.setStudyPlan(commonPlan);
    }

    private void ensureActivePlan(StudyPlan plan) {
        if (plan == null || plan.getStatus() != StudyPlanStatus.ACTIVE) {
            throw new BusinessException("An active study plan is required to study or start a study session");
        }
    }
}
