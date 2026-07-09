package com.focus.adapt.api.application.service.impl;

import com.focus.adapt.api.application.dto.StudyPlanRequest;
import com.focus.adapt.api.application.dto.StudyPlanResponse;
import com.focus.adapt.api.application.mapper.StudyPlanMapper;
import com.focus.adapt.api.application.service.StudyPlanService;
import com.focus.adapt.api.domain.entity.StudyPlan;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudyPlanStatus;
import com.focus.adapt.api.infrastructure.exception.BusinessException;
import com.focus.adapt.api.infrastructure.exception.ResourceNotFoundException;
import com.focus.adapt.api.infrastructure.repository.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudyPlanServiceImpl implements StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final StudyPlanMapper studyPlanMapper;

    @Override
    @Transactional
    public StudyPlanResponse createStudyPlan(StudyPlanRequest request, User user) {
        StudyPlan plan = studyPlanMapper.toEntity(request);
        plan.setUser(user);
        StudyPlan saved = studyPlanRepository.save(plan);
        return studyPlanMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudyPlanResponse> getMyStudyPlans(StudyPlanStatus status, User user, Pageable pageable) {
        Page<StudyPlan> plans;
        if (status == null) {
            plans = studyPlanRepository.findByUser(user, pageable);
        } else {
            plans = studyPlanRepository.findByUserAndStatus(user, status, pageable);
        }
        return plans.map(studyPlanMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public StudyPlanResponse getStudyPlanById(UUID id, User user) {
        StudyPlan plan = studyPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan not found with ID: " + id));

        if (!plan.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to view this study plan");
        }

        return studyPlanMapper.toResponse(plan);
    }

    @Override
    @Transactional
    public StudyPlanResponse updateStudyPlan(UUID id, StudyPlanRequest request, User user) {
        StudyPlan plan = studyPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan not found with ID: " + id));

        if (!plan.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to modify this study plan");
        }

        plan.setTitle(request.getTitle());
        plan.setDescription(request.getDescription());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setStatus(request.getStatus());

        StudyPlan updated = studyPlanRepository.save(plan);
        return studyPlanMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteStudyPlan(UUID id, User user) {
        StudyPlan plan = studyPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan not found with ID: " + id));

        if (!plan.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to delete this study plan");
        }

        studyPlanRepository.delete(plan);
    }
}
