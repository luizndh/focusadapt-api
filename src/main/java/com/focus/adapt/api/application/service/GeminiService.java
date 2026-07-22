package com.focus.adapt.api.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.focus.adapt.api.application.dto.MissionRequest;
import com.focus.adapt.api.application.dto.StudyPlanRequest;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.infrastructure.integration.gemini.GeminiClient;
import com.focus.adapt.api.infrastructure.integration.gemini.PromptBuilder;
import com.focus.adapt.api.infrastructure.integration.gemini.ResponseParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService  {

    private final GeminiClient geminiClient;
    private final PromptBuilder promptBuilder;
    private final ResponseParser responseParser;

    public StudyPlanRequest generateStudyPlan(String goals, User user) {
        log.info("GeminiService: Request to generate study plan");
        String prompt = promptBuilder.buildStudyPlanPrompt(goals, user);
        String rawResponse = geminiClient.generateContent(prompt);
        return responseParser.parseStudyPlan(rawResponse);
    }

    public StudyPlanRequest adaptSchedule(String currentTitle, String currentDescription, String constraint) {
        log.info("GeminiService: Request to adapt schedule");
        String prompt = promptBuilder.buildAdaptSchedulePrompt(currentTitle, currentDescription, constraint);
        String rawResponse = geminiClient.generateContent(prompt);
        return responseParser.parseStudyPlan(rawResponse);
    }

    public String generateFeedback(String sessionData, User user) {
        log.info("GeminiService: Request to generate study feedback");
        String prompt = promptBuilder.buildStudyFeedbackPrompt(sessionData, user);
        return geminiClient.generateContent(prompt);
    }

    public List<MissionRequest> generateMissions(String gamificationStatus, User user) {
        log.info("GeminiService: Request to generate missions");
        String prompt = promptBuilder.buildCreateMissionsPrompt(gamificationStatus, user);
        String rawResponse = geminiClient.generateContent(prompt);
        return responseParser.parseMissions(rawResponse);
    }

    public String adaptGamification(User user) {
        log.info("GeminiService: Request to adapt gamification settings");
        String prompt = promptBuilder.buildAdaptGamificationPrompt(user);
        return geminiClient.generateContent(prompt);
    }
}
