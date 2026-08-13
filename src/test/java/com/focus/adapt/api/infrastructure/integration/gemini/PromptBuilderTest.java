package com.focus.adapt.api.infrastructure.integration.gemini;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile;

class PromptBuilderTest {

    @Test
    void shouldBuildPromptWithProfileAndGoals() {
        PromptBuilder builder = new PromptBuilder();

        String prompt = builder.buildStudyPlanPrompt(
                "Pass my java exam",
                "Studying Java fundamentals",
                2,
                "MONDAY,TUESDAY",
                "2026-08-01",
                ProcrastinationProfile.PERFECTIONIST
        );

        assertTrue(prompt.contains("PERFECTIONIST"), "Prompt should mention procrastination type");
        assertTrue(prompt.contains("Pass my java exam"), "Prompt should contain the topic");
        assertTrue(prompt.contains("JSON"), "Prompt should require JSON output format");
    }
}
