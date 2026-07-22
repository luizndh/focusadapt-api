package com.focus.adapt.api.application.event;

import com.focus.adapt.api.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuestionnaireCompletedEvent {
    private final User user;
    private final String goals;
}
