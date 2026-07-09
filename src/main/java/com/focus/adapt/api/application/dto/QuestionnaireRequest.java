package com.focus.adapt.api.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Builder.Default
    private boolean active = true;

    @NotEmpty(message = "At least one question is required")
    @Valid
    private List<QuestionRequest> questions;
}
