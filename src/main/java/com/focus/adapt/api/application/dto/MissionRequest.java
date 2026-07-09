package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.MissionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class MissionRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Points reward is required")
    @Min(value = 1, message = "Points reward must be at least 1")
    private Integer pointsReward;

    @NotNull(message = "Mission type is required")
    private MissionType missionType;

    @NotNull(message = "Target value is required")
    @Min(value = 1, message = "Target value must be at least 1")
    private Integer targetValue;

    @Builder.Default
    private boolean active = true;
}
