package com.focus.adapt.api.application.service;

import com.focus.adapt.api.application.dto.StudyPlanRequest;
import com.focus.adapt.api.application.dto.StudyPlanResponse;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudyPlanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StudyPlanService {
    StudyPlanResponse createStudyPlan(StudyPlanRequest request, User user);
    Page<StudyPlanResponse> getMyStudyPlans(StudyPlanStatus status, User user, Pageable pageable);
    StudyPlanResponse getStudyPlanById(UUID id, User user);
    StudyPlanResponse updateStudyPlan(UUID id, StudyPlanRequest request, User user);
    void deleteStudyPlan(UUID id, User user);
}
