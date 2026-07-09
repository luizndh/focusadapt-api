package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.StudyPlanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyPlanResponse {
    private UUID id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private StudyPlanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
