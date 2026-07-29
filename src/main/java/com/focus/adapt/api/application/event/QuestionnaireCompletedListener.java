package com.focus.adapt.api.application.event;

import com.focus.adapt.api.application.dto.StudyPlanGenerateRequest;
import com.focus.adapt.api.application.service.StudyPlanService;
import com.focus.adapt.api.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionnaireCompletedListener {

    private final StudyPlanService studyPlanService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleQuestionnaireCompletedEvent(QuestionnaireCompletedEvent event) {
        User user = event.getUser();
        String goals = event.getGoals();

        log.info("Handling QuestionnaireCompletedEvent for user: {}", user.getId());

        try {
            StudyPlanGenerateRequest request = StudyPlanGenerateRequest.builder()
                    .topic(goals)
                    .availableHoursPerDay(2)
                    .build();

            studyPlanService.generateStudyPlan(request, user);
            log.info("Study plan generated successfully for user: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to generate study plan for user {}: {}", user.getId(), e.getMessage());
        }
    }
}
