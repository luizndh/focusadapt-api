package com.focus.adapt.api.application.service;

import com.focus.adapt.api.application.dto.MissionRequest;
import com.focus.adapt.api.application.dto.StudyPlanRequest;
import com.focus.adapt.api.domain.entity.User;

import java.util.List;

public interface GeminiService {

    /**
     * Generates a structured StudyPlanRequest based on user goals and procrastination profile.
     */
    StudyPlanRequest generateStudyPlan(String goals, User user);

    /**
     * Adapts an existing study plan to match constraints and timelines.
     */
    StudyPlanRequest adaptSchedule(String currentTitle, String currentDescription, String constraint);

    /**
     * Provides plain-text actionable feedback on user study history.
     */
    String generateFeedback(String sessionData, User user);

    /**
     * Generates structured new challenges/missions for the user.
     */
    List<MissionRequest> generateMissions(String gamificationStatus, User user);

    /**
     * Recommends recalibration advice for points and motivation systems.
     */
    String adaptGamification(User user);
}
