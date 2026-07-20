package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfileType;

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
public class UserQuestionnaireStatusRequest {

    @NotNull(message = "Completed flag is required")
    private Boolean completed;

    private ProcrastinationProfileType profile;
}