package com.focus.adapt.api.application.mapper;

import com.focus.adapt.api.application.dto.StudySessionRequest;
import com.focus.adapt.api.application.dto.StudySessionResponse;
import com.focus.adapt.api.domain.entity.StudySession;
import com.focus.adapt.api.domain.entity.StudyTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudySessionMapper {

    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "studyPlan", ignore = true)
    @Mapping(target = "studyTasks", ignore = true)
    StudySession toEntity(StudySessionRequest request);

    @Mapping(source = "studyPlan.id", target = "studyPlanId")
    @Mapping(source = "studyTasks", target = "studyTaskIds", qualifiedByName = "mapTasksToIds")
    StudySessionResponse toResponse(StudySession entity);

    @Named("mapTasksToIds")
    default List<UUID> mapTasksToIds(List<StudyTask> studyTasks) {
        if (studyTasks == null) {
            return Collections.emptyList();
        }
        return studyTasks.stream()
                .map(StudyTask::getId)
                .toList();
    }
}
