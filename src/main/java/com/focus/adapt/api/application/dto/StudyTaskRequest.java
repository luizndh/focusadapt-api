package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.StudyTaskStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class StudyTaskRequest {
    private String title;
    private String description;
    private UUID studyPlanId;
    private StudyTaskStatus status;
}
