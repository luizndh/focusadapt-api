package com.focus.adapt.api.application.mapper;

import com.focus.adapt.api.application.dto.StudyPlanRequest;
import com.focus.adapt.api.application.dto.StudyPlanResponse;
import com.focus.adapt.api.domain.entity.StudyPlan;
import com.focus.adapt.api.domain.entity.enums.StudyTaskStatus;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Comparator;

@Mapper(componentModel = "spring", uses = {StudyTaskMapper.class}, builder = @Builder(disableBuilder = true))
public interface StudyPlanMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "studySessions", ignore = true)
    @Mapping(target = "studyTasks", ignore = true)
    StudyPlan toEntity(StudyPlanRequest request);

    @Mapping(target = "tasks", source = "studyTasks")
    @Mapping(target = "totalTasks", ignore = true)
    @Mapping(target = "completedTasks", ignore = true)
    @Mapping(target = "progress", ignore = true)
    StudyPlanResponse toResponse(StudyPlan entity);

    @AfterMapping
    default void calculateTaskMetrics(StudyPlan entity, @MappingTarget StudyPlanResponse response) {
        if (entity.getStudyTasks() != null && !entity.getStudyTasks().isEmpty()) {
            int total = entity.getStudyTasks().size();
            long completed = entity.getStudyTasks().stream()
                    .filter(task -> task.getStatus() == StudyTaskStatus.COMPLETED)
                    .count();

            response.setTotalTasks(total);
            response.setCompletedTasks((int) completed);
            response.setProgress(total > 0 ? (double) completed / total : 0.0);

            if (response.getTasks() != null) {
                response.getTasks().sort(Comparator.comparingInt(t -> t.getSortOrder()));
            }
        } else {
            response.setTotalTasks(0);
            response.setCompletedTasks(0);
            response.setProgress(0.0);
        }
    }
}
