package com.focus.adapt.api.application.service.impl;

import com.focus.adapt.api.application.dto.StudySessionRequest;
import com.focus.adapt.api.application.dto.StudySessionResponse;
import com.focus.adapt.api.application.mapper.StudySessionMapper;
import com.focus.adapt.api.application.service.StudySessionService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudySessionServiceImpl implements StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final PomodoroSessionRepository pomodoroSessionRepository;
    private final StudySessionMapper studySessionMapper;
    private final com.focus.adapt.api.infrastructure.repository.UserRepository userRepository;
    private final com.focus.adapt.api.infrastructure.repository.PointsHistoryRepository pointsHistoryRepository;
    private final com.focus.adapt.api.infrastructure.repository.ActivityLogRepository activityLogRepository;
    private final com.focus.adapt.api.infrastructure.repository.UserMissionRepository userMissionRepository;
    private final com.focus.adapt.api.infrastructure.repository.NotificationRepository notificationRepository;
    private final com.focus.adapt.api.infrastructure.repository.UserAchievementRepository userAchievementRepository;
    private final com.focus.adapt.api.infrastructure.repository.BadgeRepository badgeRepository;

    @Override
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

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public StudySessionResponse getStudySessionById(UUID id, User user) {
        StudySession session = studySessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Study session not found: " + id));
        if (!session.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to view this study session");
        }
        return studySessionMapper.toResponse(session);
    }

    @Override
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
            processGamificationForCompletedSession(updated, user);
        }

        return studySessionMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteStudySession(UUID id, User user) {
        StudySession session = studySessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Study session not found: " + id));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to delete this study session");
        }

        studySessionRepository.delete(session);
    }

    @Override
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

    @Transactional
    public void processGamificationForCompletedSession(StudySession session, User user) {
        int pointsAwarded = 50;

        PomodoroSession pomodoro = session.getPomodoroSession();
        if (pomodoro != null && pomodoro.getStatus() == PomodoroStatus.RUNNING) {
            pomodoro.setStatus(PomodoroStatus.COMPLETED);
            pomodoro.setEndTime(LocalDateTime.now());
            pomodoroSessionRepository.save(pomodoro);
            pointsAwarded += 20;
        }

        user.setPoints(user.getPoints() + pointsAwarded);

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate lastStudy = user.getLastStudyDate();

        if (lastStudy == null) {
            user.setStreak(1);
        } else {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(lastStudy, today);
            if (daysBetween == 1) {
                user.setStreak(user.getStreak() + 1);
            } else if (daysBetween > 1) {
                user.setStreak(1);
            }
        }
        user.setLastStudyDate(today);
        userRepository.save(user);

        com.focus.adapt.api.domain.entity.PointsHistory pointsHistory = com.focus.adapt.api.domain.entity.PointsHistory.builder()
                .user(user)
                .points(pointsAwarded)
                .reason(com.focus.adapt.api.domain.entity.enums.PointsReason.STUDY_SESSION_COMPLETED)
                .description("Completed study session: " + session.getTitle())
                .build();
        pointsHistoryRepository.save(pointsHistory);

        com.focus.adapt.api.domain.entity.ActivityLog logEntry = com.focus.adapt.api.domain.entity.ActivityLog.builder()
                .user(user)
                .action("STUDY_SESSION_COMPLETE")
                .category(com.focus.adapt.api.domain.entity.enums.ActivityCategory.STUDY)
                .details("Completed study session '" + session.getTitle() + "' and earned " + pointsAwarded + " XP.")
                .build();
        activityLogRepository.save(logEntry);

        java.util.List<com.focus.adapt.api.domain.entity.UserMission> activeMissions =
                userMissionRepository.findByUserAndStatus(user, com.focus.adapt.api.domain.entity.enums.UserMissionStatus.IN_PROGRESS);

        for (com.focus.adapt.api.domain.entity.UserMission um : activeMissions) {
            com.focus.adapt.api.domain.entity.Mission m = um.getMission();
            boolean progressMade = false;

            String titleLower = m.getTitle().toLowerCase();
            if (titleLower.contains("session") || titleLower.contains("estudo") || titleLower.contains("foco")) {
                um.setCurrentValue(um.getCurrentValue() + 1);
                progressMade = true;
            } else if ((titleLower.contains("pomodoro") || titleLower.contains("timer")) && pomodoro != null) {
                um.setCurrentValue(um.getCurrentValue() + 1);
                progressMade = true;
            } else if (titleLower.contains("streak") || titleLower.contains("sequência") || titleLower.contains("dia")) {
                um.setCurrentValue(user.getStreak());
                progressMade = true;
            } else {
                um.setCurrentValue(um.getCurrentValue() + 1);
                progressMade = true;
            }

            if (progressMade) {
                if (um.getCurrentValue() >= m.getTargetValue()) {
                    um.setStatus(com.focus.adapt.api.domain.entity.enums.UserMissionStatus.COMPLETED);

                    com.focus.adapt.api.domain.entity.Notification notification = com.focus.adapt.api.domain.entity.Notification.builder()
                            .user(user)
                            .title("Mission Completed!")
                            .content("You completed the mission: " + m.getTitle() + "! Go to your dashboard to claim your points.")
                            .type(com.focus.adapt.api.domain.entity.enums.NotificationType.MISSION_COMPLETED)
                            .isRead(false)
                            .build();
                    notificationRepository.save(notification);
                }
                userMissionRepository.save(um);
            }
        }

        checkAndUnlockBadges(user);
    }

    private void checkAndUnlockBadges(User user) {
        if (user.getStreak() >= 3) {
            unlockBadgeIfUnique(user, "Consistency Starter", "Achieve a 3-day study streak");
        }
        if (user.getPoints() >= 500) {
            unlockBadgeIfUnique(user, "XP Collector", "Accumulate more than 500 XP");
        }
    }

    private void unlockBadgeIfUnique(User user, String name, String description) {
        com.focus.adapt.api.domain.entity.Badge badge = badgeRepository.findByName(name)
                .orElseGet(() -> {
                    com.focus.adapt.api.domain.entity.Badge b = com.focus.adapt.api.domain.entity.Badge.builder()
                            .name(name)
                            .description(description)
                            .iconUrl("default-badge-icon-url")
                            .build();
                    return badgeRepository.save(b);
                });

        boolean alreadyUnlocked = userAchievementRepository.existsByUserAndBadge(user, badge);
        if (!alreadyUnlocked) {
            com.focus.adapt.api.domain.entity.UserAchievement achievement = com.focus.adapt.api.domain.entity.UserAchievement.builder()
                    .user(user)
                    .badge(badge)
                    .unlockedAt(LocalDateTime.now())
                    .build();
            userAchievementRepository.save(achievement);

            com.focus.adapt.api.domain.entity.Notification notification = com.focus.adapt.api.domain.entity.Notification.builder()
                    .user(user)
                    .title("New Badge Unlocked: " + name + "!")
                    .content("Congratulations! You earned a new badge: " + description)
                    .type(com.focus.adapt.api.domain.entity.enums.NotificationType.SYSTEM)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        }
    }
}
