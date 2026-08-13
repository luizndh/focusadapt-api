package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.StudyTaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class StudyTaskResponse {
    private UUID id;
    private String title;
    private String description;
    private StudyTaskStatus status;
    private UUID studyPlanId;
    private int sortOrder;
    private int estimatedDurationMinutes;
    private String content;
    private LocalDate scheduledDate;
}
