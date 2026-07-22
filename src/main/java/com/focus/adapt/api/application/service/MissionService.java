package com.focus.adapt.api.application.service;

import com.focus.adapt.api.application.dto.MissionRequest;
import com.focus.adapt.api.application.dto.MissionResponse;
import com.focus.adapt.api.application.dto.UserMissionResponse;
import com.focus.adapt.api.application.mapper.MissionMapper;
import com.focus.adapt.api.domain.entity.Mission;
import com.focus.adapt.api.domain.entity.PointsHistory;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.UserMission;
import com.focus.adapt.api.domain.entity.enums.PointsReason;
import com.focus.adapt.api.domain.entity.enums.UserMissionStatus;
import com.focus.adapt.api.infrastructure.exception.BusinessException;
import com.focus.adapt.api.infrastructure.exception.ResourceNotFoundException;
import com.focus.adapt.api.infrastructure.repository.MissionRepository;
import com.focus.adapt.api.infrastructure.repository.PointsHistoryRepository;
import com.focus.adapt.api.infrastructure.repository.UserMissionRepository;
import com.focus.adapt.api.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final UserMissionRepository userMissionRepository;
    private final UserRepository userRepository;
    private final PointsHistoryRepository pointsHistoryRepository;
    private final MissionMapper missionMapper;

    @Transactional
    public MissionResponse createMission(MissionRequest request) {
        Mission mission = missionMapper.toEntity(request);
        Mission saved = missionRepository.save(mission);
        return missionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<MissionResponse> getMissions(Boolean activeOnly, Pageable pageable) {
        Page<Mission> missions;
        if (activeOnly != null && activeOnly) {
            missions = missionRepository.findByActive(true, pageable);
        } else {
            missions = missionRepository.findAll(pageable);
        }
        return missions.map(missionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MissionResponse getMissionById(UUID id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with ID: " + id));
        return missionMapper.toResponse(mission);
    }

    @Transactional
    public MissionResponse updateMission(UUID id, MissionRequest request) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with ID: " + id));

        mission.setTitle(request.getTitle());
        mission.setDescription(request.getDescription());
        mission.setPointsReward(request.getPointsReward());
        mission.setMissionType(request.getMissionType());
        mission.setTargetValue(request.getTargetValue());
        mission.setActive(request.isActive());

        Mission updated = missionRepository.save(mission);
        return missionMapper.toResponse(updated);
    }

    @Transactional
    public void deleteMission(UUID id) {
        if (!missionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Mission not found with ID: " + id);
        }
        missionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<UserMissionResponse> getMyMissions(UserMissionStatus status, User user, Pageable pageable) {
        Page<UserMission> userMissions;
        if (status == null) {
            userMissions = userMissionRepository.findByUser(user, pageable);
        } else {
            userMissions = userMissionRepository.findByUserAndStatus(user, status, pageable);
        }
        return userMissions.map(missionMapper::toResponse);
    }

    @Transactional
    public void claimMissionPoints(UUID userMissionId, User user) {
        UserMission userMission = userMissionRepository.findById(userMissionId)
                .orElseThrow(() -> new ResourceNotFoundException("User mission progress not found with ID: " + userMissionId));

        if (!userMission.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to claim points for this mission");
        }

        if (userMission.getStatus() == UserMissionStatus.CLAIMED) {
            throw new BusinessException("Points for this mission have already been claimed");
        }

        if (userMission.getStatus() == UserMissionStatus.IN_PROGRESS && userMission.getCurrentValue() < userMission.getMission().getTargetValue()) {
            throw new BusinessException("Mission is not completed yet");
        }

        // Complete and claim
        userMission.setStatus(UserMissionStatus.CLAIMED);
        userMission.setCompletedAt(LocalDateTime.now());
        userMissionRepository.save(userMission);

        // Add points to User
        int rewardPoints = userMission.getMission().getPointsReward();
        user.setPoints(user.getPoints() + rewardPoints);
        userRepository.save(user);

        // Record history
        PointsHistory pointsHistory = PointsHistory.builder()
                .user(user)
                .points(rewardPoints)
                .reason(PointsReason.MISSION_COMPLETED)
                .description("Completed mission: " + userMission.getMission().getTitle())
                .build();
        pointsHistoryRepository.save(pointsHistory);
    }
}
