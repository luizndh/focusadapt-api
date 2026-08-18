package com.focus.adapt.api.application.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.focus.adapt.api.application.dto.MissionRequest;
import com.focus.adapt.api.application.dto.MissionResponse;
import com.focus.adapt.api.application.dto.UserMissionResponse;
import com.focus.adapt.api.domain.entity.Mission;
import com.focus.adapt.api.domain.entity.UserMission;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface MissionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Mission toEntity(MissionRequest request);

    MissionResponse toResponse(Mission entity);

    @Mapping(source = "mission.id", target = "missionId")
    @Mapping(source = "mission.title", target = "missionTitle")
    @Mapping(source = "mission.description", target = "missionDescription")
    @Mapping(source = "mission.pointsReward", target = "pointsReward")
    @Mapping(source = "mission.targetValue", target = "targetValue")
    UserMissionResponse toResponse(UserMission entity);
}
