package com.focus.adapt.api.infrastructure.integration.gemini;

import org.springframework.stereotype.Component;

import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfileType;

@Component
public class PromptBuilder {

    public String buildStudyPlanPrompt(String topic, String description, int hoursPerDay,
                                       String availableDays, String startDate,
                                       ProcrastinationProfileType profileType) {
        String profileInstructions = getProfileInstructions(profileType);

        return String.format("""
                You are an expert academic coach. Generate a personalized study plan.

                Topic: "%s"
                Additional context: "%s"
                Available hours per day: %d
                Available days: %s
                Start date: %s

                Student profile: %s
                %s

                Requirements:
                1. Create a structured, realistic study plan with individual tasks.
                2. Each task should be a single lesson or topic to study.
                3. DO NOT generate the full content of each task — only title and a brief description.
                4. The output MUST be a valid JSON object matching this schema:
                {
                  "title": "Plan title",
                  "description": "Overview of the study plan",
                  "startDate": "YYYY-MM-DD",
                  "endDate": "YYYY-MM-DD",
                  "tasks": [
                    {
                      "title": "Task title",
                      "description": "Brief description of what to study",
                      "order": 1,
                      "estimatedDurationMinutes": 60
                    }
                  ]
                }
                Do NOT include markdown code blocks. Write ONLY the raw JSON.""",
                topic,
                description != null ? description : "N/A",
                hoursPerDay,
                availableDays != null ? availableDays : "Any day",
                startDate != null ? startDate : "Tomorrow",
                profileType.name(),
                profileInstructions
        );
    }

    public String buildStudyTaskContentPrompt(String taskTitle, String taskDescription,
                                               String planTitle,
                                               ProcrastinationProfileType profileType) {
        String profileInstructions = getProfileInstructions(profileType);

        return String.format("""
                You are an expert teacher. Generate the full study content for the following task.

                Study Plan: "%s"
                Task Title: "%s"
                Task Description: "%s"

                Student profile: %s
                %s

                Requirements:
                1. Write a complete lesson covering the topic.
                2. Include: clear explanation, practical examples, exercises, summary, and practical tips.
                3. Use markdown formatting for readability.
                4. Adapt the language and pace to the student's profile.
                5. Keep it focused and actionable — no filler content.
                6. Write the content in Portuguese (Brazil).
                7. Return ONLY the lesson content in markdown. No JSON wrapper.""",
                planTitle,
                taskTitle,
                taskDescription != null ? taskDescription : "N/A",
                profileType.name(),
                profileInstructions
        );
    }

    private String getProfileInstructions(ProcrastinationProfileType profileType) {
        return switch (profileType) {
            case PERFECTIONIST -> """
                    Profile-specific instructions:
                    - Break tasks into small, achievable steps to avoid overwhelm.
                    - Set clear, measurable objectives for each task.
                    - Use gradual progression — start easy and increase difficulty.
                    - Include encouragement to celebrate partial progress.
                    - Avoid all-or-nothing language; emphasize incremental growth.""";
            case STIMULUS_SEEKER -> """
                    Profile-specific instructions:
                    - Keep tasks dynamic and varied to maintain interest.
                    - Include quick challenges and interactive elements.
                    - Use shorter study sessions with frequent breaks.
                    - Add variety — alternate between theory, practice, and quizzes.
                    - Include gamification cues like mini-challenges and milestones.""";
        };
    }
}
