package com.focus.adapt.api.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyPlanGenerateRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    private String description;

    @Min(value = 1, message = "Available hours per day must be at least 1")
    private int availableHoursPerDay;

    private List<String> availableDays;

    private LocalDate startDate;
}
