package com.focus.adapt.api.application.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.focus.adapt.api.application.dto.StudySessionRequest;
import com.focus.adapt.api.application.dto.StudySessionResponse;
import com.focus.adapt.api.application.mapper.StudySessionMapper;
import com.focus.adapt.api.domain.entity.PomodoroSession;
import com.focus.adapt.api.domain.entity.StudyPlan;
import com.focus.adapt.api.domain.entity.StudySession;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.PomodoroStatus;
import com.focus.adapt.api.domain.entity.enums.StudySessionStatus;
import com.focus.adapt.api.infrastructure.exception.BusinessException;
import com.focus.adapt.api.infrastructure.exception.ResourceNotFoundException;
import com.focus.adapt.api.infrastructure.repository.PomodoroSessionRepository;
import com.focus.adapt.api.infrastructure.repository.StudyPlanRepository;
import com.focus.adapt.api.infrastructure.repository.StudySessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final PomodoroSessionRepository pomodoroSessionRepository;
    private final StudySessionMapper studySessionMapper;
    private final GamificationService gamificationService;

    @Transactional
    public StudySessionResponse createStudySession(StudySessionRequest request, User user) {
        StudySession session = studySessionMapper.toEntity(request);
        session.setUser(user);

        long duration = ChronoUnit.MINUTES.between(request.getStartTime(), request.getEndTime());
        session.setDurationMinutes((int) duration);

        if (request.getStudyPlanId() != null) {
            StudyPlan plan = studyPlanRepository.findById(request.getStudyPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Study plan not found: " + request.getStudyPlanId()));
            if (!plan.getUser().getId().equals(user.getId())) {
                throw new BusinessException("You do not have permission to link to this study plan");
            }
            session.setStudyPlan(plan);
        }

        StudySession saved = studySessionRepository.save(session);
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
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setStatus(request.getStatus());
        session.setNotes(request.getNotes());

        long duration = ChronoUnit.MINUTES.between(request.getStartTime(), request.getEndTime());
        session.setDurationMinutes((int) duration);

        if (request.getStudyPlanId() != null) {
            StudyPlan plan = studyPlanRepository.findById(request.getStudyPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Study plan not found: " + request.getStudyPlanId()));
            if (!plan.getUser().getId().equals(user.getId())) {
                throw new BusinessException("You do not have permission to link to this study plan");
            }
            session.setStudyPlan(plan);
        } else {
            session.setStudyPlan(null);
        }

        StudySession updated = studySessionRepository.save(session);

        if (request.getStatus() == StudySessionStatus.COMPLETED && oldStatus != StudySessionStatus.COMPLETED) {
            gamificationService.processSessionCompletion(updated, user);
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

    @Transactional
    public StudySessionResponse startPomodoro(UUID sessionId, int workDuration, int breakDuration, User user) {
        StudySession session = studySessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Study session not found: " + sessionId));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to start a Pomodoro session for this study session");
        }

        if (session.getPomodoroSession() != null) {
            throw new BusinessException("Pomodoro session already exists for this study session");
        }

        PomodoroSession pomodoro = PomodoroSession.builder()
                .user(user)
                .workDurationMinutes(workDuration)
                .breakDurationMinutes(breakDuration)
                .startTime(LocalDateTime.now())
                .status(PomodoroStatus.RUNNING)
                .build();

        PomodoroSession savedPomodoro = pomodoroSessionRepository.save(pomodoro);
        session.setPomodoroSession(savedPomodoro);

        StudySession updated = studySessionRepository.save(session);
        return studySessionMapper.toResponse(updated);
    }

}
