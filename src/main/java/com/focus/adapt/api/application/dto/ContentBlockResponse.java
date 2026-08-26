package com.focus.adapt.api.application.dto;

import java.util.UUID;

import com.focus.adapt.api.domain.entity.enums.ContentBlockType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentBlockResponse {
    private UUID id;
    private int order;
    private ContentBlockType type;
    private String title;
    private int estimatedMinutes;
    private String content;
    private boolean completed;
    private UUID studyTaskId;
}
