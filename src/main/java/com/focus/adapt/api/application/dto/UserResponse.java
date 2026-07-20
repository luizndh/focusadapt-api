package com.focus.adapt.api.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

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
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private boolean onboardingCompleted;
    private boolean questionnaireCompleted;
    private String procrastinationProfile;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
