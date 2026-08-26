package com.focus.adapt.api.infrastructure.integration.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.focus.adapt.api.application.dto.AiStudyPlanResponse;
import com.focus.adapt.api.application.dto.AiTaskBlocksResponse;
import com.focus.adapt.api.infrastructure.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResponseParser {

    private final ObjectMapper objectMapper;

    public AiStudyPlanResponse parseStudyPlanWithTasks(String rawResponse) {
        String cleanJson = cleanJsonResponse(rawResponse);
        try {
            return objectMapper.readValue(cleanJson, AiStudyPlanResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse StudyPlan with tasks from JSON: {}", cleanJson, e);
            throw new BusinessException("AI generated plan did not conform to the required JSON schema");
        }
    }

    public AiTaskBlocksResponse parseTaskBlocksStructure(String rawResponse) {
        String cleanJson = cleanJsonResponse(rawResponse);
        try {
            return objectMapper.readValue(cleanJson, AiTaskBlocksResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse ContentBlocks structure from JSON: {}", cleanJson, e);
            throw new BusinessException("AI generated blocks did not conform to the required JSON schema");
        }
    }

    /**
     * Helper to clean markdown block wrappers (e.g. ```json ... ```) that models sometimes return.
     */
    private String cleanJsonResponse(String response) {
        if (response == null) {
            return "{}";
        }
        String cleaned = response.trim();
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline != -1) {
                cleaned = cleaned.substring(firstNewline + 1);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
        }
        return cleaned.trim();
    }
}
