package com.focus.adapt.api.infrastructure.integration.gemini;

import org.springframework.stereotype.Component;

import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile;

@Component
public class PromptBuilder {

    public String buildStudyPlanPrompt(String category, String description, int hoursPerDay,
                                       String availableDays, String startDate,
                                       ProcrastinationProfile profileType) {
        String profileInstructions = getPlanProfileInstructions(profileType);

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
                Do not use Markdown code fences (e.g. ```json).
                Do not include explanations outside the JSON.

                Schema:

                {
                    "title": "Java Streams",
                    "description": "A structured study plan for mastering Java Streams from basics to advanced collectors.",
                    "category": "Programming",
                    "startDate": "YYYY-MM-DD",
                    "endDate": "YYYY-MM-DD",
                    "totalEstimatedMinutes": 180,
                    "tasks": [
                        {
                            "title": "Stream Pipelines and Sources",
                            "description": "Understand stream creation from collections and the lifecycle of source, intermediate, and terminal operations.",
                            "order": 1,
                            "estimatedDurationMinutes": 25
                        },
                        {
                            "title": "Filtering and Slicing with filter, distinct, limit",
                            "description": "Learn how to filter elements and slice stream data using core intermediate operations.",
                            "order": 2,
                            "estimatedDurationMinutes": 20
                        }
                    ]
                }
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
        String profileInstructions = getContentProfileInstructions(profileType);

        return String.format("""
                You are an expert educator creating high-quality, standalone educational lessons for an e-learning mobile application.

                Study Plan: "%s"
                Task Title: "%s"
                Task Description: "%s"
                Student Procrastination Profile: %s

                %s

                ==================================================
                CRITICAL CONTENT AND FORMAT RULES
                =================================

                1. FORMAT: Return ONLY pure, well-structured Markdown in Portuguese (Brazil - pt-BR).
                   Do NOT wrap in JSON. Do NOT include markdown code blocks around the entire text.

                2. NATURE OF THE CONTENT:
                   * This is a STATIC, STANDALONE LESSON read directly in the application screen.
                   * The user is reading a textbook/article format. There is NO chat interface and NO conversational back-and-forth.

                3. STRICT PROHIBITIONS (DO NOT VIOLATE):
                   * NEVER use conversational phrases or chatbot-style dialogue (e.g., "Quer continuar agora ou fazer uma pausa?", "Se for continuar, peça...", "Pergunte-me se quiser...", "O que você achou?", "Espero que tenha gostado!", "Me diga se ficou com dúvida").
                   * NEVER instruct the user to type messages, prompt the AI, or request additional code/examples.
                   * NEVER leave lessons incomplete or dependent on future prompts. Everything needed must be self-contained in this lesson.

                4. REQUIRED LESSON STRUCTURE:
                   The lesson MUST be thorough, clear, engaging, and structured with the following sections:

                   # %s

                   ## 1. Introdução e Objetivo
                   Explain what this topic is, why it matters, and what the student will achieve by the end of this lesson.

                   ## 2. Conceito Central
                   Detailed, crystal-clear explanation of the core concept. Use analogies or bullet points where helpful.

                   ## 3. Como Funciona na Prática (com exemplos)
                   Provide complete, realistic, and concrete step-by-step examples. Explain what each key part of the code does.

                   ## 4. Casos de Uso Comuns e Armadilhas
                   Show real-world scenarios where this is used, along with common mistakes students make and how to avoid them.

                   ## 5. Exercício Prático de Fixação
                   Provide 1 practical challenge or question for the student to test their knowledge, followed immediately by the detailed solution and explanation inside a collapsible/clear solution block.

                   ## 6. Resumo e Pontos-Chave
                   A bulleted summary of the 3-5 most critical takeaways.

                   ## 7. Dica de Ouro
                   A valuable best-practice tip, mnemonic, or industry insight related to this topic.

                5. READING TIME: The lesson should take approximately 10 to 15 minutes of focused reading and practice.
                """,
            planTitle,
            taskTitle,
            taskDescription != null ? taskDescription : "N/A",
            profileType.name(),
            profileInstructions,
            taskTitle
        );
    }

    private String getPlanProfileInstructions(ProcrastinationProfile profileType) {
        return switch (profileType) {
            case PERFECTIONIST -> """
                    Profile-specific planning guidelines:
                    - Favor smaller, clearly defined tasks (15-25 minutes).
                    - Establish a gradual learning curve without intimidating complexity.
                    - Define precise, achievable learning outcomes for each task.""";
            case STIMULUS_SEEKER -> """
                    Profile-specific planning guidelines:
                    - Keep tasks focused, varied, and direct (20-30 minutes).
                    - Mix conceptual topics with practical application topics.
                    - Avoid overly theoretical tasks; focus on applied knowledge.""";
        };
    }

    private String getContentProfileInstructions(ProcrastinationProfile profileType) {
        return switch (profileType) {
            case PERFECTIONIST -> """
                    PERSONALIZED LEARNING STRATEGY — PERFECTIONIST

                    The learner may struggle with fear of mistakes, excessive self-criticism,
                    difficulty starting and cognitive overload.

                    Structure the lesson to reduce psychological and cognitive barriers:

                    - Divide the lesson into small, clearly defined learning sections.
                    - Introduce one major concept at a time.
                    - Use a clear progression from basic to more advanced ideas.
                    - Provide explicit learning objectives for each major section.
                    - Use step-by-step examples.
                    - Prefer guided exercises before open-ended challenges.
                    - Include frequent checkpoints that reinforce what was learned.
                    - Normalize mistakes as part of learning.
                    - Avoid unnecessarily difficult or ambiguous exercises.
                    - Avoid long uninterrupted explanations.
                    - Make progress and completion points explicit.
                    - Do not use excessive motivational language.

                    The lesson should feel structured, predictable and achievable.""";
            case STIMULUS_SEEKER -> """
                    PERSONALIZED LEARNING STRATEGY — STIMULUS SEEKER

                    The learner may lose interest quickly, seek novelty and respond better
                    to frequent changes in activity and immediate feedback.

                    Structure the lesson as a sequence of short learning blocks:

                    - Prefer learning blocks of approximately 5–10 minutes.
                    - Avoid long uninterrupted theoretical sections.
                    - Alternate between explanation, examples and active practice.
                    - Introduce a quiz, recall question, flashcard or mini-challenge
                    frequently when appropriate.
                    - Prefer concrete examples before abstract explanations.
                    - Vary activity formats throughout the lesson.
                    - Provide immediate feedback after interactive activities.
                    - Create frequent, meaningful completion points.
                    - Keep instructions short and direct.
                    - Avoid repetitive exercises.
                    - Use visible progress markers between learning blocks.
                    - Use small challenges to maintain engagement, but do not sacrifice
                    conceptual correctness or educational depth.

                    The lesson should feel dynamic, varied and continuously progressive.""";
        };
    }
}
