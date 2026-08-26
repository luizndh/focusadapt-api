package com.focus.adapt.api.infrastructure.integration.gemini;

import org.springframework.stereotype.Component;

import com.focus.adapt.api.domain.entity.enums.ContentBlockType;
import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile;

@Component
public class PromptBuilder {

    public String buildStudyPlanPrompt(String category, String description, int availableMinutesPerDay,
                                       String availableDays, String startDate,
                                       ProcrastinationProfile profileType) {
        String profileInstructions = getPlanProfileInstructions(profileType);
        String dailyTimeFormatted = formatDailyStudyTime(availableMinutesPerDay);

        return String.format("""
                You are an expert academic curriculum designer and study planner.

                Your goal is to create a personalized, high-quality study curriculum for the student.

                ==================================================
                FUNDAMENTAL ARCHITECTURE & TASK DEFINITION
                ==================================================

                * The STUDY PLAN represents the overall subject the student wants to learn.
                * Each STUDY TASK represents a single, coherent ACADEMIC TOPIC or CONCEPT within that subject.
                * A STUDY TASK is NOT an exercise, challenge, quiz, mission, reward, or gamification activity.
                * The title of each StudyTask must describe ONLY the academic concept itself (e.g. "Filtragem com filter()", NOT "Quiz de filter()", NOT "Desafio 1").
                * Do NOT put "Day 1", "Day 2", dates, weekdays, or motivational slogans in StudyTask titles or descriptions.
                * Pedagogical activities (quizzes, exercises, checkpoints, challenges) will be generated later inside ContentBlocks, NOT in the plan definition.

                Student Information:
                * Main topic: "%s"
                * Additional context: "%s"
                * Available study time per day: %s
                * Available days of the week: %s
                * Start date: %s
                * Procrastination Profile: %s

                ==================================================
                PROFILE-SPECIFIC CURRICULUM STRUCTURING
                ==================================================

                Both profiles study the same fundamental academic concepts. The curriculum structure must adapt to the student's behavioral characteristics:

                %s

                ==================================================
                STUDY TASK DURATION & GRANULARITY
                ==================================================

                * Every StudyTask must represent approximately 15 to 45 minutes of focused study.
                * NEVER create massive tasks of hundreds of minutes.
                * If a concept is too broad, divide it into logically cohesive sub-concepts.
                * Do not artificially fragment a single simple concept into multiple trivial tasks.

                ==================================================
                SCHEDULE & CALENDAR RULES
                ==================================================

                * Distribute the study tasks strictly according to the student's declared parameters.
                * The first task must be scheduled on or after the declared start date, matching one of the student's available days of the week.
                * Assign a valid, sequential `scheduledDate` (in YYYY-MM-DD format) to each StudyTask, scheduling tasks ONLY on the student's available days of the week.
                * Do NOT exceed the declared daily study time on any single calendar date.
                * Set the overall plan `startDate` (date of the first task) and `endDate` (date of the final task) accurately based on the schedule.

                ==================================================
                SUBJECT CATEGORY SELECTION
                ==================================================

                The "category" field must be chosen from the following controlled list:
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

                ==================================================
                OUTPUT FORMAT
                ==================================================

                Return ONLY valid JSON without Markdown code fences (no ```json).
                Do not include explanations or text outside the JSON object.

                Schema:
                {
                    "title": "Topic Name",
                    "description": "Overview of the study plan curriculum.",
                    "category": "One Category From List",
                    "startDate": "YYYY-MM-DD",
                    "endDate": "YYYY-MM-DD",
                    "totalEstimatedDurationMinutes": 180,
                    "tasks": [
                        {
                            "title": "Academic Concept Title",
                            "description": "Brief explanation of the academic topic covered.",
                            "order": 1,
                            "estimatedDurationMinutes": 30,
                            "scheduledDate": "YYYY-MM-DD"
                        }
                    ]
                }
                """,
            category,
            description != null ? description : "N/A",
            dailyTimeFormatted,
            availableDays != null ? availableDays : "Any day",
            startDate != null ? startDate : "Tomorrow",
            profileType.name(),
            profileInstructions
        );
    }

    /**
     * Constrói o prompt para geração de plano de estudos a partir de um documento PDF.
     * Reutiliza as mesmas regras acadêmicas, de duração (15-45m), perfil comportamental e calendário.
     */
    public String buildStudyPlanFromPdfPrompt(String topicHint, String description, int availableMinutesPerDay,
                                              String availableDays, String startDate,
                                              ProcrastinationProfile profileType) {
        String profileInstructions = getPlanProfileInstructions(profileType);
        String dailyTimeFormatted = formatDailyStudyTime(availableMinutesPerDay);

        return String.format("""
                You are an expert academic curriculum designer and study planner analyzing the attached PDF document.

                Your goal is to extract the core subject matter, chapters, concepts, and learning objectives from the attached PDF document and organize them into a personalized, high-quality study curriculum for the student.

                ==================================================
                PDF CONTENT ANALYSIS INSTRUCTIONS
                ==================================================

                * Analyze the text, visual structure, chapters, sections, diagrams, and tables of the attached PDF.
                * Structure the study plan following the logical and progressive curriculum presented in the PDF.
                * If a topic hint is provided, use it to prioritize relevant sections; otherwise, determine the title from the document.

                ==================================================
                FUNDAMENTAL ARCHITECTURE & TASK DEFINITION
                ==================================================

                * The STUDY PLAN represents the overall subject covered in the PDF.
                * Each STUDY TASK represents a single, coherent ACADEMIC TOPIC or CONCEPT extracted from the PDF.
                * A STUDY TASK is NOT an exercise, challenge, quiz, mission, reward, or gamification activity.
                * The title of each StudyTask must describe ONLY the academic concept itself (e.g. "Estruturas de Repetição", NOT "Capítulo 1", NOT "Quiz").
                * Do NOT put "Day 1", "Day 2", dates, weekdays, or motivational slogans in StudyTask titles or descriptions.
                * Pedagogical activities (quizzes, exercises, checkpoints, challenges) will be generated later inside ContentBlocks, NOT in the plan definition.

                Student Information:
                * Topic Hint: "%s"
                * Additional context: "%s"
                * Available study time per day: %s
                * Available days of the week: %s
                * Start date: %s
                * Procrastination Profile: %s

                ==================================================
                PROFILE-SPECIFIC CURRICULUM STRUCTURING
                ==================================================

                Both profiles study the same fundamental academic concepts. The curriculum structure must adapt to the student's behavioral characteristics:

                %s

                ==================================================
                STUDY TASK DURATION & GRANULARITY
                ==================================================

                * Every StudyTask must represent approximately 15 to 45 minutes of focused study.
                * NEVER create massive tasks of hundreds of minutes.
                * If a chapter or concept in the PDF is broad, divide it into logically cohesive sub-concepts.
                * Do not artificially fragment a single simple concept into multiple trivial tasks.

                ==================================================
                SCHEDULE & CALENDAR RULES
                ==================================================

                * Distribute the study tasks strictly according to the student's declared parameters.
                * The first task must be scheduled on or after the declared start date, matching one of the student's available days of the week.
                * Assign a valid, sequential `scheduledDate` (in YYYY-MM-DD format) to each StudyTask, scheduling tasks ONLY on the student's available days of the week.
                * Do NOT exceed the declared daily study time on any single calendar date.
                * Set the overall plan `startDate` (date of the first task) and `endDate` (date of the final task) accurately based on the schedule.

                ==================================================
                SUBJECT CATEGORY SELECTION
                ==================================================

                The "category" field must be chosen from the following controlled list:
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

                ==================================================
                OUTPUT FORMAT
                ==================================================

                Return ONLY valid JSON without Markdown code fences (no ```json).
                Do not include explanations or text outside the JSON object.

                Schema:
                {
                    "title": "Topic Name Extracted From PDF",
                    "description": "Overview of the study plan curriculum based on the PDF content.",
                    "category": "One Category From List",
                    "startDate": "YYYY-MM-DD",
                    "endDate": "YYYY-MM-DD",
                    "totalEstimatedDurationMinutes": 180,
                    "tasks": [
                        {
                            "title": "Academic Concept Title",
                            "description": "Brief explanation of the academic topic covered in the PDF.",
                            "order": 1,
                            "estimatedDurationMinutes": 30,
                            "scheduledDate": "YYYY-MM-DD"
                        }
                    ]
                }
                """,
            topicHint != null ? topicHint : "Extracted from PDF",
            description != null ? description : "Based on attached PDF document",
            dailyTimeFormatted,
            availableDays != null ? availableDays : "Any day",
            startDate != null ? startDate : "Tomorrow",
            profileType.name(),
            profileInstructions
        );
    }

    private String formatDailyStudyTime(int minutes) {
        if (minutes < 60) {
            return minutes + " minutes";
        }
        int hours = minutes / 60;
        int rem = minutes % 60;
        if (rem == 0) {
            return hours + (hours == 1 ? " hour" : " hours") + " (" + minutes + " minutes)";
        }
        return hours + (hours == 1 ? " hour and " : " hours and ") + rem + " minutes (" + minutes + " minutes total)";
    }

    /**
     * ETAPA 1: Gera a estrutura pedagógica de ContentBlocks para uma StudyTask (sem gerar o texto completo).
     */
    public String buildTaskBlocksStructurePrompt(String taskTitle, String taskDescription,
                                                 String planTitle, ProcrastinationProfile profileType) {
        String structureGuidelines = getBlocksStructureProfileGuidelines(profileType);

        return String.format("""
                You are an expert instructional designer structuring the learning blocks (ContentBlocks) for a specific academic study task.

                Context:
                * Study Plan: "%s"
                * Academic StudyTask: "%s"
                * Task Description: "%s"
                * Student Procrastination Profile: %s

                ==================================================
                CONTENT BLOCK ARCHITECTURE
                ==================================================

                A StudyTask represents what the student learns. ContentBlocks represent how the lesson is delivered in bite-sized chunks.

                Possible Block Types:
                * EXPLANATION: Conceptual explanation, theories, or principles.
                * EXAMPLE: Practical code examples, walk-throughs, or concrete illustrations.
                * GUIDED_EXERCISE: Step-by-step guided practice with solutions.
                * QUIZ: Quick questions with immediate feedback.
                * FLASHCARD: Active recall question/answer pairs.
                * CHALLENGE: Micro-challenge or problem to solve.
                * CHECKPOINT: Milestone self-check to confirm understanding.
                * SUMMARY: Key takeaways and wrap-up.

                ==================================================
                PROFILE-SPECIFIC STRUCTURING STRATEGY
                ==================================================

                %s

                ==================================================
                RULES
                ==================================================

                1. Each block should have an estimated duration of 5 to 10 minutes.
                2. The titles of the blocks must be clear, engaging, and in Portuguese (pt-BR).
                3. Do NOT generate the full lesson content text in this step. Only generate the block structure.

                ==================================================
                OUTPUT FORMAT
                ==================================================

                Return ONLY valid JSON (no markdown code fences).

                Schema:
                {
                    "blocks": [
                        {
                            "order": 1,
                            "type": "EXPLANATION",
                            "title": "Título do Bloco em Português",
                            "estimatedMinutes": 5
                        },
                        {
                            "order": 2,
                            "type": "EXAMPLE",
                            "title": "Título do Bloco em Português",
                            "estimatedMinutes": 5
                        }
                    ]
                }
                """,
            planTitle,
            taskTitle,
            taskDescription != null ? taskDescription : "N/A",
            profileType.name(),
            structureGuidelines
        );
    }

    /**
     * ETAPA 2: Gera o conteúdo em Markdown de um único ContentBlock sob demanda.
     */
    public String buildBlockContentPrompt(String planTitle, String taskTitle, String taskDescription,
                                          String blockTitle, ContentBlockType blockType,
                                          ProcrastinationProfile profileType) {
        String typeGuidelines = getBlockTypeSpecificGuidelines(blockType);
        String profilePedagogy = getBlockContentProfilePedagogy(profileType);

        return String.format("""
                You are an expert educator generating the content for a single, focused learning block (ContentBlock) in an e-learning application.

                Lesson Context:
                * Study Plan: "%s"
                * StudyTask Topic: "%s"
                * Task Description: "%s"
                * ContentBlock Title: "%s"
                * ContentBlock Type: %s
                * Student Procrastination Profile: %s

                ==================================================
                BLOCK TYPE GUIDELINES: %s
                ==================================================

                %s

                ==================================================
                PROFILE PEDAGOGICAL STRATEGY: %s
                ==================================================

                %s

                ==================================================
                FORMAT & DELIVERY RULES
                ==================================================

                1. Output ONLY pure, well-formatted Markdown in Portuguese (Brazil - pt-BR).
                2. Do NOT wrap in JSON. Do NOT include markdown code blocks around the entire output.
                3. Start directly with `# %s`.
                4. Be direct, clear, and high-value. This block should take approximately 5 to 10 minutes to read/complete.
                5. NEVER use conversational chatbot phrases (e.g. "Espero que tenha gostado", "Me diga se tem dúvidas", "Quer continuar?").
                """,
            planTitle,
            taskTitle,
            taskDescription != null ? taskDescription : "N/A",
            blockTitle,
            blockType.name(),
            profileType.name(),
            blockType.name(),
            typeGuidelines,
            profileType.name(),
            profilePedagogy,
            blockTitle
        );
    }

    private String getPlanProfileInstructions(ProcrastinationProfile profileType) {
        return switch (profileType) {
            case PERFECTIONIST -> """
                    PERFECTIONIST PROFILE CURRICULUM GUIDELINES:
                    * The student experiences fear of failure, self-criticism, difficulty starting, and potential overwhelm from broad topics.
                    * Structure the curriculum with a clear, gradual progression from fundamentals to advanced topics.
                    * Define precise, clearly bounded academic scopes for each task so the student has clear endpoints.
                    * Avoid combining multiple disparate learning objectives into a single StudyTask.
                    * Keep individual task durations within 15 to 45 minutes to prevent cognitive fatigue and analysis paralysis.""";
            case STIMULUS_SEEKER -> """
                    STIMULUS_SEEKER PROFILE CURRICULUM GUIDELINES:
                    * The student experiences rapid boredom, seeks novelty, and responds strongly to frequent progress markers and practical application.
                    * Keep tasks sharply focused on concrete concepts, alternating between foundational theory and immediate practical applications.
                    * Avoid long consecutive blocks of purely abstract theory where practical progression is possible.
                    * Maintain strict academic titles (e.g., 'Filtragem com filter()', never 'Quiz de filter()' or 'Jogo de Streams').
                    * Keep individual task durations within 15 to 45 minutes with clear milestones.""";
        };
    }

    private String getBlocksStructureProfileGuidelines(ProcrastinationProfile profileType) {
        return switch (profileType) {
            case PERFECTIONIST -> """
                    PERFECTIONIST STRUCTURE PREFERENCES:
                    * Emphasize linear progression, predictability, and psychological safety.
                    * Suggested flow: EXPLANATION -> EXAMPLE -> GUIDED_EXERCISE -> CHECKPOINT -> EXAMPLE -> QUIZ -> SUMMARY.
                    * Ensure every concept has a step-by-step example and guided exercise before any assessment.
                    * Use CHECKPOINT blocks to validate understanding and reduce ambiguity.""";
            case STIMULUS_SEEKER -> """
                    STIMULUS_SEEKER STRUCTURE PREFERENCES:
                    * Emphasize brevity, novelty, frequent activity switching, and immediate feedback.
                    * Suggested flow: EXPLANATION -> EXAMPLE -> QUIZ -> CHALLENGE -> FLASHCARD -> EXAMPLE -> SUMMARY.
                    * Interleave short theoretical blocks with interactive micro-challenges, quizzes, and active recall flashcards.
                    * Keep all blocks punchy (5-7 minutes each).""";
        };
    }

    private String getBlockTypeSpecificGuidelines(ContentBlockType blockType) {
        return switch (blockType) {
            case EXPLANATION -> """
                    * Provide a crystal-clear, structured explanation of the core concept.
                    * Use analogies, bullet points, and clean syntax highlights.
                    * Explain the 'why' and 'how' with zero unnecessary fluff.""";
            case EXAMPLE -> """
                    * Provide realistic, clean, and fully commented code/practical examples.
                    * Break down the example line by line or step by step.
                    * Show expected outputs or results clearly.""";
            case GUIDED_EXERCISE -> """
                    * Present a practical problem scenario and guide the student.
                    * Format strictly using the following sections:
                      ### Enunciado
                      [Descrição do problema e objetivo prático]
                      ### Dica
                      [Dica para orientar o início do raciocínio]
                      ### Resolução
                      [Código ou passo a passo completo comentado]""";
            case QUIZ -> """
                    * Provide 2 to 3 concise multiple choice questions.
                    * Format each question strictly as:
                      ### Questão 1
                      [Enunciado da pergunta]
                      - (A) [Opção A]
                      - (B) [Opção B]
                      - (C) [Opção C]
                      - (D) [Opção D]
                      **Resposta Correta:** A
                      **Explicação:** [Explicação concisa do porquê está correta e as outras erradas]""";
            case FLASHCARD -> """
                    * Provide 2 to 4 active recall flashcards.
                    * Format each card strictly as:
                      ### Card 1
                      **Frente:** [Pergunta ou conceito chave para recordar]
                      **Verso:** [Resposta direta, definição essencial ou exemplo chave]""";
            case CHALLENGE -> """
                    * Present a concise micro-challenge or practical problem to solve.
                    * Format strictly using the following sections:
                      ### Desafio
                      [Descrição do desafio e requisitos]
                      ### Dica
                      [Dica útil para começar]
                      ### Resolução
                      [Código completo e explicação da solução]""";
            case CHECKPOINT -> """
                    * A self-assessment checkpoint with 3 to 4 verification criteria.
                    * Format as:
                      ### Verificação de Compreensão
                      - [ ] [Critério 1 que o aluno deve ter compreendido]
                      - [ ] [Critério 2 que o aluno deve ter compreendido]
                      - [ ] [Critério 3 que o aluno deve ter compreendido]
                      **Mensagem:** [Mensagem encorajadora sobre o progresso]""";
            case SUMMARY -> """
                    * 3 to 5 bullet points summarizing the most vital takeaways.
                    * A 'Dica de Ouro' (Golden Rule / Pro Tip) for real-world application.""";
        };
    }

    private String getBlockContentProfilePedagogy(ProcrastinationProfile profileType) {
        return switch (profileType) {
            case PERFECTIONIST -> """
                    * Maintain high clarity and predictability.
                    * Normalize mistakes and demystify edge cases.
                    * Use step-by-step guidance and structured, achievable milestones.""";
            case STIMULUS_SEEKER -> """
                    * Keep text dynamic, concise, and focused on active engagement.
                    * Use concrete examples before abstract theory.
                    * Deliver immediate feedback and actionable takeaways.""";
        };
    }
}
