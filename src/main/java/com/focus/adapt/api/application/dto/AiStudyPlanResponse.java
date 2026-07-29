package com.focus.adapt.api.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiStudyPlanResponse {

    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<AiStudyTaskItem> tasks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiStudyTaskItem {
        private String title;
        private String description;
        private int order;
        private int estimatedDurationMinutes;
    }
}
