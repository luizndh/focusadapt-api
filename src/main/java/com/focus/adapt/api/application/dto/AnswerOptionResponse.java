package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfileType;
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
public class AnswerOptionResponse {
    private UUID id;
    private String text;
    private int value;
    private ProcrastinationProfileType procrastinationProfileType;
}
