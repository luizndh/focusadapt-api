package com.focus.adapt.api.application.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiStudyPlanResponse {

    private String title;
    private String description;
    private String category;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalEstimatedDurationMinutes;
    private List<AiStudyTaskItem> tasks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiStudyTaskItem {
        private String title;
        private String description;
        private int order;
        private int estimatedDurationMinutes;
        private LocalDate scheduledDate;
    }
}
