package com.focus.adapt.api.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.focus.adapt.api.application.dto.StudySessionRequest;
import com.focus.adapt.api.application.dto.StudySessionResponse;
import com.focus.adapt.api.application.mapper.StudySessionMapper;
import com.focus.adapt.api.domain.entity.PomodoroSession;
import com.focus.adapt.api.domain.entity.StudySession;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.PomodoroStatus;
import com.focus.adapt.api.domain.entity.enums.StudySessionStatus;
import com.focus.adapt.api.infrastructure.repository.PomodoroSessionRepository;
import com.focus.adapt.api.infrastructure.repository.StudySessionRepository;

@ExtendWith(MockitoExtension.class)
class StudySessionServiceTest {

    @Mock
    private StudySessionRepository studySessionRepository;

    @Mock
    private PomodoroSessionRepository pomodoroSessionRepository;

    @Mock
    private StudySessionMapper studySessionMapper;

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
                .lastStudyDate(LocalDate.now().minusDays(1)) // Studied yesterday, should increment streak to 3
                .build();
        user.setId(userId);

        StudySession session = StudySession.builder()
                .title("Database Design")
                .status(StudySessionStatus.SCHEDULED)
                .user(user)
                .pomodoroSession(PomodoroSession.builder()
                        .workDurationMinutes(25)
                        .status(PomodoroStatus.RUNNING)
                        .build())
                .build();

        when(studySessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        StudySessionRequest request = StudySessionRequest.builder()
                .title("Database Design")
                .status(StudySessionStatus.COMPLETED)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .build();

        when(studySessionRepository.save(any(StudySession.class))).thenReturn(session);
        when(studySessionMapper.toResponse(any(StudySession.class))).thenReturn(new StudySessionResponse());

        // Execute update
        studySessionService.updateStudySession(sessionId, request, user);

        // Verify GamificationService was called instead of the individual repositories
        verify(gamificationService, times(1)).processSessionCompletion(any(StudySession.class), any(User.class));
    }
}
