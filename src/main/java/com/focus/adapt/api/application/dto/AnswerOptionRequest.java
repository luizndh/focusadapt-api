package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfileType;
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
public class AnswerOptionRequest {

    @NotBlank(message = "Option text is required")
    private String text;

    @NotNull(message = "Value/weight is required")
    private Integer value;

    private ProcrastinationProfileType procrastinationProfileType;
}
