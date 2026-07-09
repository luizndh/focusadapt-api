package com.focus.adapt.api.application.service;

import com.focus.adapt.api.application.dto.MissionRequest;
import com.focus.adapt.api.application.dto.MissionResponse;
import com.focus.adapt.api.application.dto.UserMissionResponse;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.UserMissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MissionService {
    MissionResponse createMission(MissionRequest request);
    Page<MissionResponse> getMissions(Boolean activeOnly, Pageable pageable);
    MissionResponse getMissionById(UUID id);
    MissionResponse updateMission(UUID id, MissionRequest request);
    void deleteMission(UUID id);
    Page<UserMissionResponse> getMyMissions(UserMissionStatus status, User user, Pageable pageable);
    void claimMissionPoints(UUID userMissionId, User user);
}
