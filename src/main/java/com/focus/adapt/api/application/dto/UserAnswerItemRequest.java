package com.focus.adapt.api.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnswerItemRequest {

    @NotNull(message = "Question ID is required")
    private UUID questionId;

    @NotNull(message = "Selected option ID is required")
    private UUID selectedOptionId;
}
