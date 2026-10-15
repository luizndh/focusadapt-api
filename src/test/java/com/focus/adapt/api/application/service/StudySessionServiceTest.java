package com.focus.adapt.api.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.focus.adapt.api.application.dto.BadgeResponse;
import com.focus.adapt.api.application.dto.StudySessionRequest;
import com.focus.adapt.api.application.dto.StudySessionResponse;
import com.focus.adapt.api.application.mapper.BadgeMapper;
import com.focus.adapt.api.application.mapper.StudySessionMapper;
import com.focus.adapt.api.domain.entity.Badge;
import com.focus.adapt.api.domain.entity.StudyPlan;
import com.focus.adapt.api.domain.entity.StudySession;
import com.focus.adapt.api.domain.entity.StudyTask;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.BadgeCategory;
import com.focus.adapt.api.domain.entity.enums.StudyMode;
import com.focus.adapt.api.domain.entity.enums.StudyPlanStatus;
import com.focus.adapt.api.domain.entity.enums.StudySessionStatus;
import com.focus.adapt.api.infrastructure.exception.BusinessException;
import com.focus.adapt.api.infrastructure.repository.StudySessionRepository;
import com.focus.adapt.api.infrastructure.repository.StudyTaskRepository;

@ExtendWith(MockitoExtension.class)
class StudySessionServiceTest {

    @Mock
    private StudySessionRepository studySessionRepository;

    @Mock
    private StudyTaskRepository studyTaskRepository;

    @Mock
    private StudySessionMapper studySessionMapper;

    @Mock
    private BadgeMapper badgeMapper;

    @Mock
    private GamificationService gamificationService;

    @InjectMocks
    private StudySessionService studySessionService;

    @Test
    void shouldCompleteStudySessionAndProcessGamification() {
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .points(100)
                .streak(2)
                .lastStudyDate(LocalDate.now().minusDays(1))
                .build();
        user.setId(userId);

        StudySession session = StudySession.builder()
                .title("Database Design")
                .status(StudySessionStatus.IN_PROGRESS)
                .studyMode(StudyMode.POMODORO)
                .user(user)
                .workDurationMinutes(25)
                .breakDurationMinutes(5)
                .xpAwarded(0)
                .build();

        StudyPlan plan = StudyPlan.builder().user(user).status(StudyPlanStatus.ACTIVE).build();
        plan.setId(UUID.randomUUID());
        StudyTask task = StudyTask.builder().studyPlan(plan).build();
        UUID taskId = UUID.randomUUID();
        task.setId(taskId);

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(studyTaskRepository.findById(taskId)).thenReturn(Optional.of(task));

        StudySessionRequest request = StudySessionRequest.builder()
                .title("Database Design")
                .studyMode(StudyMode.POMODORO)
                .status(StudySessionStatus.COMPLETED)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .studyTaskIds(List.of(taskId))
                .build();

        Badge badge = Badge.builder()
                .code("STUDY_TASK_1")
                .name("Primeiro Passo")
                .category(BadgeCategory.STUDY_TASK)
                .threshold(1)
                .xpReward(20)
                .build();
        badge.setId(UUID.randomUUID());

        BadgeResponse badgeResponse = BadgeResponse.builder()
                .id(badge.getId())
                .code(badge.getCode())
                .name(badge.getName())
                .category(badge.getCategory())
                .threshold(badge.getThreshold())
                .xpReward(badge.getXpReward())
                .build();

        when(studySessionRepository.save(any(StudySession.class))).thenReturn(session);
        when(gamificationService.processSessionCompletion(any(StudySession.class), any(User.class)))
                .thenReturn(new com.focus.adapt.api.application.dto.SessionGamificationResult(50, 20, 70, List.of(badge)));
        when(badgeMapper.toResponse(badge)).thenReturn(badgeResponse);
        when(studySessionMapper.toResponse(any(StudySession.class))).thenReturn(new StudySessionResponse());

        StudySessionResponse response = studySessionService.updateStudySession(sessionId, request, user);

        assertNotNull(response);
        verify(gamificationService, times(1)).processSessionCompletion(any(StudySession.class), any(User.class));
        verify(studySessionRepository, times(1)).flush();
        verify(studyTaskRepository, times(1)).flush();
        assertEquals(50, response.getSessionXp());
        assertEquals(20, response.getBadgeBonusXp());
        assertEquals(70, response.getTotalXp());
        assertEquals(70, response.getXpEarned());
        assertFalse(response.getUnlockedBadges().isEmpty());
        assertEquals("STUDY_TASK_1", response.getUnlockedBadges().get(0).getCode());
    }

    @Test
    void shouldNotProcessGamificationAgainIfSessionAlreadyHasXpAwarded() {
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = User.builder().build();
        user.setId(userId);

        StudySession session = StudySession.builder()
                .title("Already Processed")
                .status(StudySessionStatus.COMPLETED)
                .studyMode(StudyMode.FREE)
                .user(user)
                .xpAwarded(120)
                .build();

        StudyPlan plan = StudyPlan.builder().user(user).status(StudyPlanStatus.ACTIVE).build();
        plan.setId(UUID.randomUUID());
        StudyTask task = StudyTask.builder().studyPlan(plan).build();
        UUID taskId = UUID.randomUUID();
        task.setId(taskId);

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(studyTaskRepository.findById(taskId)).thenReturn(Optional.of(task));

        StudySessionRequest request = StudySessionRequest.builder()
                .title("Already Processed")
                .studyMode(StudyMode.FREE)
                .status(StudySessionStatus.COMPLETED)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .studyTaskIds(List.of(taskId))
                .build();

        when(studySessionRepository.save(any(StudySession.class))).thenReturn(session);
        when(studySessionMapper.toResponse(any(StudySession.class))).thenReturn(new StudySessionResponse());

        studySessionService.updateStudySession(sessionId, request, user);

        verify(gamificationService, never()).processSessionCompletion(any(StudySession.class), any(User.class));
    }

    @Test
    void shouldCreateCompletedStudySessionAndProcessGamification() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().build();
        user.setId(userId);

        StudyPlan plan = StudyPlan.builder().user(user).status(StudyPlanStatus.ACTIVE).build();
        plan.setId(UUID.randomUUID());
        StudyTask task = StudyTask.builder().studyPlan(plan).build();
        UUID taskId = UUID.randomUUID();
        task.setId(taskId);

        StudySessionRequest request = StudySessionRequest.builder()
                .title("New Completed Session")
                .studyMode(StudyMode.POMODORO)
                .status(StudySessionStatus.COMPLETED)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(50))
                .studyTaskIds(List.of(taskId))
                .completedCycles(2)
                .effectiveStudyMinutes(50)
                .build();

        StudySession entity = StudySession.builder()
                .title("New Completed Session")
                .status(StudySessionStatus.COMPLETED)
                .studyMode(StudyMode.POMODORO)
                .user(user)
                .xpAwarded(0)
                .build();

        when(studySessionMapper.toEntity(request)).thenReturn(entity);
        when(studyTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(studySessionRepository.save(any(StudySession.class))).thenReturn(entity);
        when(gamificationService.processSessionCompletion(any(StudySession.class), any(User.class)))
                .thenReturn(com.focus.adapt.api.application.dto.SessionGamificationResult.empty());
        when(studySessionMapper.toResponse(any(StudySession.class))).thenReturn(new StudySessionResponse());

        StudySessionResponse response = studySessionService.createStudySession(request, user);

        assertNotNull(response);
        verify(gamificationService, times(1)).processSessionCompletion(any(StudySession.class), any(User.class));
    }

    @Test
    void shouldRejectSessionForInactiveStudyPlan() {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        User user = User.builder().build();
        user.setId(userId);

        StudyPlan plan = StudyPlan.builder().user(user).status(StudyPlanStatus.ARCHIVED).build();
        plan.setId(UUID.randomUUID());
        StudyTask task = StudyTask.builder().studyPlan(plan).build();
        task.setId(taskId);

        StudySessionRequest request = StudySessionRequest.builder()
                .title("Database Design")
                .studyMode(StudyMode.FREE)
                .status(StudySessionStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .studyTaskIds(List.of(taskId))
                .build();

        when(studySessionMapper.toEntity(request)).thenReturn(StudySession.builder().build());
        when(studyTaskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(BusinessException.class, () -> studySessionService.createStudySession(request, user));
    }
}
