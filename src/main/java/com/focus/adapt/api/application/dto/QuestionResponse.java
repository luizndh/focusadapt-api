package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {
    private UUID id;
    private String text;
    private int orderNumber;
    private QuestionType questionType;
    private List<AnswerOptionResponse> options;
}
