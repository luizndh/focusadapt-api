package com.focus.adapt.api.application.dto;

import com.focus.adapt.api.domain.entity.enums.BadgeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private BadgeCategory category;
    private int threshold;
    private int xpReward;
    private String iconUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
