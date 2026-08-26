package com.focus.adapt.api.application.dto;

import java.util.List;

import com.focus.adapt.api.domain.entity.enums.ContentBlockType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiTaskBlocksResponse {

    private List<AiBlockItem> blocks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiBlockItem {
        private int order;
        private ContentBlockType type;
        private String title;
        private int estimatedMinutes;
    }
}
