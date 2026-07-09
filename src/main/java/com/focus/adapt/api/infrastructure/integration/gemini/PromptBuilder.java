package com.focus.adapt.api.infrastructure.integration.gemini;

import org.springframework.stereotype.Component;

import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfileType;

@Component
public class PromptBuilder {

    public String buildStudyPlanPrompt(String goals, User user) {
        ProcrastinationProfileType profileType = user.getProcrastinationProfile() != null
                ? user.getProcrastinationProfile().getProfileType()
                : ProcrastinationProfileType.STIMULUS_SEEKER; // Default profile if none is set

        return String.format("""
                             You are an expert academic coach. Generate a personalized study plan for a student with the profile '%s' (procrastination type).
                             The student's goals are: "%s".
                             Requirements:
                             1. Keep it structured, action-oriented, and realistic.
                             2. The output MUST be a valid JSON object matching this schema:
                             {
                               "title": "Plan title here",
                               "description": "Detailed description of the roadmap",
                               "startDate": "YYYY-MM-DD" (Default: tomorrow),
                               "endDate": "YYYY-MM-DD" (Default: 30 days from start)
                             }
                             Do NOT include any markdown code blocks (like ```json), write ONLY the raw JSON string.""",
                profileType.name(), goals
        );
    }

    public String buildAdaptSchedulePrompt(String currentPlanTitle, String currentPlanDesc, String constraint) {
        return String.format("""
                             You are an expert study planner. Adapt the following study plan to fit a new constraint.
                             Current Plan Title: "%s"
                             Current Plan Description: "%s"
                             Constraint to apply: "%s"
                             Requirements:
                             1. Re-schedule study slots and change plan scope to fit the constraint.
                             2. The output MUST be a valid JSON object matching this schema:
                             {
                               "title": "Adapted Plan title",
                               "description": "Adapted plan description details including rescheduled timeline",
                               "startDate": "YYYY-MM-DD",
                               "endDate": "YYYY-MM-DD"
                             }
                             Do NOT include any markdown code blocks, write ONLY the raw JSON string.""",
                currentPlanTitle, currentPlanDesc, constraint
        );
    }

    public String buildStudyFeedbackPrompt(String sessionData, User user) {
        String profileName = user.getProcrastinationProfile() != null
                ? user.getProcrastinationProfile().getProfileType().name()
                : "Standard";

        return String.format("""
                             You are a supportive academic mentor. Analyze the following study sessions completed by a student with the profile '%s':
                             %s
                             Provide brief, encouraging, and highly actionable feedback in plain text. Mention positive points and suggest 1-2 small habits to overcome procrastination. Max 150 words.""",
                profileName, sessionData
        );
    }

    public String buildCreateMissionsPrompt(String gamificationStatus, User user) {
        String profileName = user.getProcrastinationProfile() != null
                ? user.getProcrastinationProfile().getProfileType().name()
                : "Standard";

        return String.format("""
                             You are a game designer for an education app. Create 3 personalized gamification missions for a student with the profile '%s'.
                             Current Gamification status/context: "%s".
                             Requirements:
                             1. Each mission must challenge the user to build focus habits.
                             2. The output MUST be a valid JSON array matching this schema:
                             [
                               {
                                 "title": "Mission Title",
                                 "description": "Actionable challenge description (e.g., complete 3 sessions of 25 min)",
                                 "pointsReward": 100 (Integer between 50 and 500),
                                 "missionType": "One of: STUDY_SESSION, POMODORO, QUESTIONNAIRE, STREAK",
                                 "targetValue": 3 (Integer value to achieve)
                               }
                             ]
                             Do NOT include any markdown code blocks, write ONLY the raw JSON string.""",
                profileName, gamificationStatus
        );
    }

    public String buildAdaptGamificationPrompt(User user) {
        String profileName = user.getProcrastinationProfile() != null
                ? user.getProcrastinationProfile().getProfileType().name()
                : "Standard";
        int currentPoints = user.getPoints();

        return String.format("""
                             You are a game systems designer. Recalibrate the gamification settings for a student with the procrastination profile '%s' (current points: %d).
                             Provide a brief text recommendation (Max 100 words) on how to adjust point rates, reward spacing, or level thresholds to keep the student motivated and engaged.""",
                profileName, currentPoints
        );
    }
}
