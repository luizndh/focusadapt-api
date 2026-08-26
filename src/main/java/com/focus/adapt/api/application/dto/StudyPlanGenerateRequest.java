package com.focus.adapt.api.application.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyPlanGenerateRequest {

    @NotBlank(message = "Topic is required")
    private String topic;

    private String description;

    @Min(value = 15, message = "Available minutes per day must be at least 15")
    private Integer availableMinutesPerDay;

    private Integer availableHoursPerDay;

    private List<String> availableDays;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    public int getEffectiveMinutesPerDay() {
        if (availableMinutesPerDay != null && availableMinutesPerDay >= 15) {
            return availableMinutesPerDay;
        }
        if (availableHoursPerDay != null && availableHoursPerDay > 0) {
            return availableHoursPerDay * 60;
        }
        return 60;
    }

    public LocalDate getEffectiveStartDate() {
        return startDate != null ? startDate : LocalDate.now().plusDays(1);
    }

    public String getFormattedAvailableDays() {
        if (availableDays != null && !availableDays.isEmpty()) {
            return String.join(",", availableDays);
        }
        return "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY";
    }
}
