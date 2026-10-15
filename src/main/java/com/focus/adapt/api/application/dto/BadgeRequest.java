package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.BadgeCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
public class BadgeRequest {

    @NotBlank(message = "Badge code is required")
    private String code;

    @NotBlank(message = "Badge name is required")
    private String name;

    @NotBlank(message = "Badge description is required")
    private String description;

    @NotNull(message = "Badge category is required")
    private BadgeCategory category;

    @NotNull(message = "Threshold is required")
    @Positive(message = "Threshold must be greater than zero")
    private Integer threshold;

    @NotNull(message = "XP reward is required")
    @PositiveOrZero(message = "XP reward must be greater than or equal to zero")
    private Integer xpReward;

    @NotBlank(message = "Icon URL is required")
    private String iconUrl;
}
