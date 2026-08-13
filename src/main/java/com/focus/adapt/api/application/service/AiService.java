package com.focus.adapt.api.application.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.focus.adapt.api.application.dto.AiStudyPlanResponse;
import com.focus.adapt.api.domain.entity.StudyTask;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile;
import com.focus.adapt.api.infrastructure.integration.gemini.GeminiClient;
import com.focus.adapt.api.infrastructure.integration.gemini.PromptBuilder;
import com.focus.adapt.api.infrastructure.integration.gemini.ResponseParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final GeminiClient geminiClient;
    private final PromptBuilder promptBuilder;
    private final ResponseParser responseParser;

    public AiStudyPlanResponse generateStudyPlan(String topic, String description,
                                                  int hoursPerDay, List<String> availableDays,
                                                  LocalDate startDate, User user) {
        log.info("AiService: Generating study plan for topic '{}'", topic);

        ProcrastinationProfile profileType = resolveProfileType(user);
        String daysFormatted = availableDays != null ? String.join(", ", availableDays) : null;
        String startDateStr = startDate != null ? startDate.toString() : null;

        String prompt = promptBuilder.buildStudyPlanPrompt(
                topic, description, hoursPerDay, daysFormatted, startDateStr, profileType
        );

        String rawResponse = geminiClient.generateContent(prompt);
        log.debug("Raw response from Gemini: " + rawResponse);
        return responseParser.parseStudyPlanWithTasks(rawResponse);
    }

    public String generateStudyTaskContent(StudyTask task, User user) {
        log.info("AiService: Generating content for task '{}'", task.getTitle());

        ProcrastinationProfile profileType = resolveProfileType(user);
        String planTitle = task.getStudyPlan() != null ? task.getStudyPlan().getTitle() : "Study Plan";

        String prompt = promptBuilder.buildStudyTaskContentPrompt(
                task.getTitle(), task.getDescription(), planTitle, profileType
        );

        return geminiClient.generateContent(prompt);
    }

    private ProcrastinationProfile resolveProfileType(User user) {
        if (user.getProcrastinationProfile() != null) {
            return user.getProcrastinationProfile();
        }
        return ProcrastinationProfile.STIMULUS_SEEKER;
    }
}
