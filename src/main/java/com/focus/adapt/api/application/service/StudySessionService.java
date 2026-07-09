package com.focus.adapt.api.application.service;

import com.focus.adapt.api.application.dto.StudySessionRequest;
import com.focus.adapt.api.application.dto.StudySessionResponse;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudySessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StudySessionService {
    StudySessionResponse createStudySession(StudySessionRequest request, User user);
    Page<StudySessionResponse> getMyStudySessions(StudySessionStatus status, User user, Pageable pageable);
    StudySessionResponse getStudySessionById(UUID id, User user);
    StudySessionResponse updateStudySession(UUID id, StudySessionRequest request, User user);
    void deleteStudySession(UUID id, User user);
    StudySessionResponse startPomodoro(UUID sessionId, int workDuration, int breakDuration, User user);
}
