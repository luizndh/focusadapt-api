package com.focus.adapt.api.infrastructure.integration.gemini;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.focus.adapt.api.domain.entity.enums.ContentBlockType;
import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile;

class PromptBuilderTest {

    private final PromptBuilder builder = new PromptBuilder();

    @Test
    void shouldBuildStudyPlanPromptForPerfectionist() {
        String prompt = builder.buildStudyPlanPrompt(
                "Java Streams",
                "Studying Java fundamentals",
                15,
                "MONDAY,TUESDAY",
                "2026-08-01",
                ProcrastinationProfile.PERFECTIONIST
        );

        assertTrue(prompt.contains("PERFECTIONIST"), "Prompt should mention perfectionist profile");
        assertTrue(prompt.contains("Java Streams"), "Prompt should contain the topic");
        assertTrue(prompt.contains("15 minutos"), "Prompt should contain available minutes");
        assertTrue(prompt.contains("scheduledDate"), "Prompt schema should include scheduledDate");
        assertTrue(prompt.contains("15 a 45 minutos"), "Prompt should enforce duration guidelines");
        assertTrue(prompt.contains("progressão gradual"), "Prompt should include perfectionist curriculum guidelines");
        assertTrue(prompt.contains("História"), "Prompt should include História category");
        assertTrue(prompt.contains("Geografia"), "Prompt should include Geografia category");
        assertTrue(prompt.contains("Biologia"), "Prompt should include Biologia category");
        assertTrue(prompt.contains("Sociologia"), "Prompt should include Sociologia category");
        assertTrue(prompt.contains("Filosofia"), "Prompt should include Filosofia category");
        assertTrue(prompt.contains("Química"), "Prompt should include Química category");
        assertTrue(prompt.contains("Tecnologia da Informação"), "Prompt should include Tecnologia da Informação category");
    }

    @Test
    void shouldBuildStudyPlanPromptForStimulusSeeker() {
        String prompt = builder.buildStudyPlanPrompt(
                "Java Streams",
                "Studying Java fundamentals",
                120,
                "MONDAY,TUESDAY",
                "2026-08-01",
                ProcrastinationProfile.STIMULUS_SEEKER
        );

        assertTrue(prompt.contains("STIMULUS_SEEKER"), "Prompt should mention stimulus seeker profile");
        assertTrue(prompt.contains("Java Streams"), "Prompt should contain the topic");
        assertTrue(prompt.contains("2 horas"), "Prompt should contain available hours/minutes");
        assertTrue(prompt.contains("scheduledDate"), "Prompt schema should include scheduledDate");
        assertTrue(prompt.contains("alternando entre teoria fundamental e aplicações práticas imediatas"), "Prompt should include stimulus seeker curriculum guidelines");
    }

    @Test
    void shouldBuildTaskBlocksStructurePromptForPerfectionist() {
        String prompt = builder.buildTaskBlocksStructurePrompt(
                "Operações Intermediárias",
                "Aprender filter e map",
                "Java Streams",
                ProcrastinationProfile.PERFECTIONIST
        );

        assertTrue(prompt.contains("PERFECTIONIST"));
        assertTrue(prompt.contains("CHECKPOINT"));
        assertTrue(prompt.contains("GUIDED_EXERCISE"));
        assertTrue(prompt.contains("JSON"));
    }

    @Test
    void shouldBuildTaskBlocksStructurePromptForStimulusSeeker() {
        String prompt = builder.buildTaskBlocksStructurePrompt(
                "Operações Intermediárias",
                "Aprender filter e map",
                "Java Streams",
                ProcrastinationProfile.STIMULUS_SEEKER
        );

        assertTrue(prompt.contains("STIMULUS_SEEKER"));
        assertTrue(prompt.contains("CHALLENGE"));
        assertTrue(prompt.contains("FLASHCARD"));
        assertTrue(prompt.contains("QUIZ"));
    }

    @Test
    void shouldBuildBlockContentPromptForExplanation() {
        String prompt = builder.buildBlockContentPrompt(
                "Java Streams",
                "Filtragem com filter()",
                "Conceitos de filtragem",
                "O que é filter()?",
                ContentBlockType.EXPLANATION,
                ProcrastinationProfile.PERFECTIONIST
        );

        assertTrue(prompt.contains("EXPLANATION"));
        assertTrue(prompt.contains("Filtragem com filter()"));
        assertTrue(prompt.contains("O que é filter()?"));
        assertTrue(prompt.contains("Markdown"));
    }

    @Test
    void shouldBuildBlockContentPromptForQuiz() {
        String prompt = builder.buildBlockContentPrompt(
                "Java Streams",
                "Filtragem com filter()",
                "Conceitos de filtragem",
                "Quiz Rápido de Predicates",
                ContentBlockType.QUIZ,
                ProcrastinationProfile.STIMULUS_SEEKER
        );

        assertTrue(prompt.contains("QUIZ"));
        assertTrue(prompt.contains("STIMULUS_SEEKER"));
    }

    @Test
    void shouldBuildStudyPlanFromPdfPromptForPerfectionist() {
        String prompt = builder.buildStudyPlanFromPdfPrompt(
                "Redes de Computadores",
                "Capítulos 1 ao 4",
                30,
                "MONDAY,WEDNESDAY,FRIDAY",
                "2026-09-01",
                ProcrastinationProfile.PERFECTIONIST
        );

        assertTrue(prompt.contains("documento PDF anexado"));
        assertTrue(prompt.contains("Redes de Computadores"));
        assertTrue(prompt.contains("PERFECTIONIST"));
        assertTrue(prompt.contains("scheduledDate"));
        assertTrue(prompt.contains("30 minutos"));
        assertTrue(prompt.contains("progressão gradual"));
    }

    @Test
    void shouldBuildStudyPlanFromPdfPromptForStimulusSeeker() {
        String prompt = builder.buildStudyPlanFromPdfPrompt(
                "Estruturas de Dados",
                "Árvores e Grafos",
                60,
                "TUESDAY,THURSDAY",
                "2026-09-01",
                ProcrastinationProfile.STIMULUS_SEEKER
        );

        assertTrue(prompt.contains("documento PDF anexado"));
        assertTrue(prompt.contains("Estruturas de Dados"));
        assertTrue(prompt.contains("STIMULUS_SEEKER"));
        assertTrue(prompt.contains("1 hora (60 minutos)"));
        assertTrue(prompt.contains("alternando entre teoria fundamental e aplicações práticas imediatas"));
    }
}
