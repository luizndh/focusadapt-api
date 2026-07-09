package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class QuestionRequest {

    @NotBlank(message = "Question text is required")
    private String text;

    @NotNull(message = "Order number is required")
    private Integer orderNumber;

    @NotNull(message = "Question type is required")
    private QuestionType questionType;

    @NotEmpty(message = "At least one answer option is required")
    @Valid
    private List<AnswerOptionRequest> options;
}
