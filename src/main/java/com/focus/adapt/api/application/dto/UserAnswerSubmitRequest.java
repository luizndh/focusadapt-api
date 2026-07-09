package com.focus.adapt.api.application.dto;

import jakarta.validation.Valid;
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
public class UserAnswerSubmitRequest {

    @NotEmpty(message = "Answers list cannot be empty")
    @Valid
    private List<UserAnswerItemRequest> answers;

    @jakarta.validation.constraints.NotBlank(message = "Goals cannot be blank")
    private String goals;
}
