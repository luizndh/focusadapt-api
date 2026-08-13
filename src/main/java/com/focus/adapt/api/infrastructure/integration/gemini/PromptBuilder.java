package com.focus.adapt.api.infrastructure.integration.gemini;

import org.springframework.stereotype.Component;

import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile;

@Component
public class PromptBuilder {

    public String buildStudyPlanPrompt(String category, String description, int hoursPerDay,
                                       String availableDays, String startDate,
                                       ProcrastinationProfile profileType) {
        String profileInstructions = getProfileInstructions(profileType);

        return String.format("""
                You are an expert academic curriculum designer and study planner.

                Your goal is to create a personalized study curriculum for the student.

                The study plan represents a structured path for learning the MAIN TOPIC.

                IMPORTANT DISTINCTION:

                * The STUDY PLAN represents the overall subject the student wants to learn.
                * Each STUDY TASK represents one academic topic or concept within that subject.
                * A STUDY TASK is NOT an exercise, challenge, quiz, mission, reward, or study activity.
                * Motivational strategies and activities must not be placed in the StudyTask title or description.
                * Those strategies will be applied later when the student studies the task.

                Student information:

                Main topic: "%s"
                Additional context: "%s"
                Available hours per day: %d
                Available days: %s
                Start date: %s

                Student procrastination profile: %s

                %s

                ==================================================
                STUDY PLAN RULES
                ================

                1. The StudyPlan title must be the MAIN TOPIC itself.

                Example:
                Input topic: "Java Streams"
                Plan title: "Java Streams"

                Do not add motivational phrases, dates, difficulty labels, or profile-specific wording to the title.

                2. The StudyPlan "category" field must represent a BROAD SUBJECT CATEGORY useful for grouping different study plans.

                Choose exactly ONE category from:

                * Programming
                * Backend Development
                * Frontend Development
                * Mobile Development
                * Databases
                * DevOps
                * Cloud Computing
                * Data Science
                * Artificial Intelligence
                * Mathematics
                * Physics
                * Languages
                * Business
                * Other

                Example:
                "Java Streams" -> "Programming"
                "Spring Boot" -> "Backend Development"
                "PostgreSQL" -> "Databases"

                Do not create a new category unless none of the provided categories are appropriate.

                3. The description must explain what the student will learn and what the plan covers.

                4. Break the main topic into logically ordered academic concepts.

                5. Each StudyTask must represent ONE coherent concept, topic, or lesson.

                6. StudyTasks must follow a logical learning progression from fundamentals to more advanced concepts.

                7. Do NOT put "Day 1", "Day 2", dates, weekdays, or schedule information in StudyTask titles.

                8. Do NOT put exercises, challenges, quizzes, missions, rewards, or gamification mechanics in StudyTask titles or descriptions.

                9. Avoid overlapping tasks.

                10. Avoid tasks that are too broad.

                Bad:
                "Learn Java Streams"

                Good:
                "Stream Pipelines"
                "Intermediate and Terminal Operations"
                "Filtering with filter()"
                "Mapping with map()"
                "Flattening with flatMap()"
                "Reduction with reduce()"
                "Collectors"
                "Parallel Streams"

                11. The number of tasks must be appropriate for the available study time.

                12. Do not artificially create many small tasks only to fill the schedule.

                13. Do not merge unrelated concepts simply to reduce the number of tasks.

                ==================================================
                PERSONALIZATION RULES
                =====================

                The student's procrastination profile affects HOW the curriculum should be structured, but NOT what the academic topics are.

                PERFECTIONIST:

                * Prefer smaller and clearly scoped academic topics.
                * Create a gradual progression.
                * Avoid tasks that combine too many concepts.
                * Prefer frequent milestones and clear completion points.
                * Do not make tasks artificially difficult.
                * Do not add motivational slogans to task titles.

                STIMULUS_SEEKER:

                * Keep tasks focused and relatively short.
                * Prefer a varied sequence of related concepts.
                * Avoid long theoretical blocks when the subject allows practical progression.
                * Alternate conceptual topics and practical topics where appropriate.
                * Do not turn the StudyTask itself into a challenge or game.
                * Challenges, quizzes, rewards, and dynamic activities must be generated later during the study session, not inside the StudyTask definition.

                ==================================================
                SCHEDULE RULES
                ==============

                Use the student's available days and hours per day to determine:

                * number of tasks;
                * estimated duration of each task;
                * overall start and end dates.

                The schedule must fit within the user's declared availability.

                Do not assign more study time than the user has available.

                Do not assume availability on days that were not provided.

                ==================================================
                STUDY TASK INFORMATION
                ======================

                For every StudyTask provide:

                * title
                * description
                * order
                * estimatedDurationMinutes

                The title must identify the academic concept itself.

                The description must briefly explain what the student will learn.

                ==================================================
                OUTPUT FORMAT
                =============

                Return ONLY valid JSON.

                Do not use Markdown.
                Do not use code fences.
                Do not include explanations outside the JSON.

                Schema:

                {
                    "title": "Java Streams",
                    "description": "A structured study plan for learning Java Streams...",
                    "category": "Programming",
                    "startDate": "YYYY-MM-DD",
                    "endDate": "YYYY-MM-DD",
                    "totalEstimatedMinutes": 600,
                    "tasks": [
                        {
                            "title": "Stream Pipelines",
                            "description": "Understand the structure of a stream pipeline and how source, intermediate operations, and terminal operations work together.",
                            "order": 1,
                            "estimatedDurationMinutes": 60
                        }
                    ]
                }

                Ensure that:

                * title is the exact main topic;
                * category is exactly one of the allowed categories;
                * tasks represent academic concepts, not activities;
                * tasks are ordered from foundational to advanced concepts;
                * the schedule fits the user's availability;
                * the JSON is syntactically valid.
                """,
            category,
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
                                               ProcrastinationProfile profileType) {
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

    private String getProfileInstructions(ProcrastinationProfile profileType) {
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
