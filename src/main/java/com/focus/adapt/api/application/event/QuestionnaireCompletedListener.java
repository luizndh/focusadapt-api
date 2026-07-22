package com.focus.adapt.api.application.event;

import com.focus.adapt.api.application.dto.MissionRequest;
import com.focus.adapt.api.application.dto.StudyPlanRequest;
import com.focus.adapt.api.application.mapper.MissionMapper;
import com.focus.adapt.api.application.mapper.StudyPlanMapper;
import com.focus.adapt.api.application.service.GeminiService;
import com.focus.adapt.api.domain.entity.Mission;
import com.focus.adapt.api.domain.entity.StudyPlan;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.UserMission;
import com.focus.adapt.api.domain.entity.enums.UserMissionStatus;
import com.focus.adapt.api.infrastructure.repository.MissionRepository;
import com.focus.adapt.api.infrastructure.repository.StudyPlanRepository;
import com.focus.adapt.api.infrastructure.repository.UserMissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionnaireCompletedListener {

    private final GeminiService geminiService;
    private final StudyPlanRepository studyPlanRepository;
    private final MissionRepository missionRepository;
    private final UserMissionRepository userMissionRepository;
    private final StudyPlanMapper studyPlanMapper;
    private final MissionMapper missionMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleQuestionnaireCompletedEvent(QuestionnaireCompletedEvent event) {
        User user = event.getUser();
        String goals = event.getGoals();

        log.info("Handling QuestionnaireCompletedEvent for user: {}", user.getId());

        try {
            StudyPlanRequest planRequest = geminiService.generateStudyPlan(goals, user);
            if (planRequest != null) {
                StudyPlan plan = studyPlanMapper.toEntity(planRequest);
                plan.setUser(user);
                studyPlanRepository.save(plan);
            }
        } catch (Exception e) {
            log.error("Gemini failed to generate study plan: {}", e.getMessage());
        }

        try {
            List<MissionRequest> missionRequests = geminiService.generateMissions("Initial diagnostic questionnaire setup", user);
            if (missionRequests != null) {
                for (MissionRequest mr : missionRequests) {
                    Mission m = missionMapper.toEntity(mr);
                    Mission savedMission = missionRepository.save(m);

                    UserMission um = UserMission.builder()
                            .user(user)
                            .mission(savedMission)
                            .status(UserMissionStatus.IN_PROGRESS)
                            .currentValue(0)
                            .build();
                    userMissionRepository.save(um);
                }
            }
        } catch (Exception e) {
            log.error("Gemini failed to generate missions: {}", e.getMessage());
        }
    }
}
