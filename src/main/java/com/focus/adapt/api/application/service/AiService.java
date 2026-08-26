package com.focus.adapt.api.application.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.focus.adapt.api.application.dto.AiStudyPlanResponse;
import com.focus.adapt.api.application.dto.AiTaskBlocksResponse;
import com.focus.adapt.api.domain.entity.ContentBlock;
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
                                                  int availableMinutesPerDay, List<String> availableDays,
                                                  LocalDate startDate, User user) {
        log.info("AiService: Generating study plan for topic '{}' ({} minutes/day)", topic, availableMinutesPerDay);

        ProcrastinationProfile profileType = resolveProfileType(user);
        String daysFormatted = availableDays != null ? String.join(", ", availableDays) : null;
        String startDateStr = startDate != null ? startDate.toString() : null;

        String prompt = promptBuilder.buildStudyPlanPrompt(
                topic, description, availableMinutesPerDay, daysFormatted, startDateStr, profileType
        );

        String rawResponse = geminiClient.generateContent(prompt);
        log.debug("Raw response from Gemini: {}", rawResponse);
        return responseParser.parseStudyPlanWithTasks(rawResponse);
    }

    public AiStudyPlanResponse generateStudyPlanFromPdf(byte[] pdfBytes, String topicHint, String description,
                                                        int availableMinutesPerDay, List<String> availableDays,
                                                        LocalDate startDate, User user) {
        log.info("AiService: Generating study plan from PDF ({} bytes, {} minutes/day)",
                pdfBytes != null ? pdfBytes.length : 0, availableMinutesPerDay);

        ProcrastinationProfile profileType = resolveProfileType(user);
        String daysFormatted = availableDays != null ? String.join(", ", availableDays) : null;
        String startDateStr = startDate != null ? startDate.toString() : null;

        String prompt = promptBuilder.buildStudyPlanFromPdfPrompt(
                topicHint, description, availableMinutesPerDay, daysFormatted, startDateStr, profileType
        );

        String rawResponse = geminiClient.generateContentWithPdf(prompt, pdfBytes);
        log.debug("Raw response from Gemini for PDF: {}", rawResponse);
        return responseParser.parseStudyPlanWithTasks(rawResponse);
    }

    /**
     * ETAPA 1: Gera a estrutura dos ContentBlocks para a StudyTask.
     */
    public AiTaskBlocksResponse generateTaskBlocksStructure(StudyTask task, User user) {
        log.info("AiService: Generating ContentBlocks structure for task '{}'", task.getTitle());

        ProcrastinationProfile profileType = resolveProfileType(user);
        String planTitle = task.getStudyPlan() != null ? task.getStudyPlan().getTitle() : "Study Plan";

        String prompt = promptBuilder.buildTaskBlocksStructurePrompt(
                task.getTitle(), task.getDescription(), planTitle, profileType
        );

        String rawResponse = geminiClient.generateContent(prompt);
        log.debug("Raw blocks structure from Gemini: {}", rawResponse);
        return responseParser.parseTaskBlocksStructure(rawResponse);
    }

    /**
     * ETAPA 2: Gera o conteúdo em Markdown para um ContentBlock específico sob demanda.
     */
    public String generateBlockContent(StudyTask task, ContentBlock block, User user) {
        log.info("AiService: Generating content for ContentBlock '{}' (type: {}) of task '{}'",
                block.getTitle(), block.getBlockType(), task.getTitle());

        ProcrastinationProfile profileType = resolveProfileType(user);
        String planTitle = task.getStudyPlan() != null ? task.getStudyPlan().getTitle() : "Study Plan";

        String prompt = promptBuilder.buildBlockContentPrompt(
                planTitle, task.getTitle(), task.getDescription(),
                block.getTitle(), block.getBlockType(), profileType
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
