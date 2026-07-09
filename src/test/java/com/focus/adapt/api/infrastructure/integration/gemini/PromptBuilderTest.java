package com.focus.adapt.api.infrastructure.integration.gemini;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.focus.adapt.api.domain.entity.ProcrastinationProfile;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfileType;

class PromptBuilderTest {

    @Test
    void shouldBuildPromptWithProfileAndGoals() {
        PromptBuilder builder = new PromptBuilder();

        ProcrastinationProfile profile = ProcrastinationProfile.builder()
                .profileType(ProcrastinationProfileType.PERFECTIONIST)
                .build();

        User user = User.builder()
                .name("John")
                .procrastinationProfile(profile)
                .build();

        String prompt = builder.buildStudyPlanPrompt("Pass my java exam", user);

        assertTrue(prompt.contains("PERFECTIONIST"), "Prompt should mention procrastination type");
        assertTrue(prompt.contains("Pass my java exam"), "Prompt should contain the user's specific goals");
        assertTrue(prompt.contains("JSON"), "Prompt should require JSON output format");
    }
}
