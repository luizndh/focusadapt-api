package com.focus.adapt.api.infrastructure.integration.gemini;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.focus.adapt.api.application.dto.MissionRequest;
import com.focus.adapt.api.application.dto.StudyPlanRequest;
import com.focus.adapt.api.infrastructure.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResponseParser {

    private final ObjectMapper objectMapper;

    public StudyPlanRequest parseStudyPlan(String rawResponse) {
        String cleanJson = cleanJsonResponse(rawResponse);
        try {
            return objectMapper.readValue(cleanJson, StudyPlanRequest.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse StudyPlan from JSON: {}", cleanJson, e);
            throw new BusinessException("AI generated plan did not conform to the required JSON schema");
        }
    }

    public List<MissionRequest> parseMissions(String rawResponse) {
        String cleanJson = cleanJsonResponse(rawResponse);
        try {
            return objectMapper.readValue(cleanJson, new TypeReference<List<MissionRequest>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Missions from JSON: {}", cleanJson, e);
            throw new BusinessException("AI generated missions did not conform to the required JSON schema");
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
            // Find first newline
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
