package com.focus.adapt.api.application.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.focus.adapt.api.application.dto.AiStudyPlanResponse;
import com.focus.adapt.api.application.dto.StudyPlanGenerateRequest;
import com.focus.adapt.api.application.dto.StudyPlanRequest;
import com.focus.adapt.api.application.dto.StudyPlanResponse;
import com.focus.adapt.api.application.mapper.StudyPlanMapper;
import com.focus.adapt.api.domain.entity.StudyPlan;
import com.focus.adapt.api.domain.entity.StudyTask;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.StudyPlanStatus;
import com.focus.adapt.api.domain.entity.enums.StudyTaskStatus;
import com.focus.adapt.api.infrastructure.exception.BusinessException;
import com.focus.adapt.api.infrastructure.exception.ResourceNotFoundException;
import com.focus.adapt.api.infrastructure.repository.StudyPlanRepository;
import com.focus.adapt.api.infrastructure.repository.StudyTaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final StudyPlanMapper studyPlanMapper;
    private final AiService aiService;
    private final StudyTaskRepository studyTaskRepository;

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

    @Transactional(readOnly = true)
    public StudyPlanResponse getStudyPlanById(UUID id, User user) {
        StudyPlan plan = studyPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan not found with ID: " + id));

        if (!plan.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to view this study plan");
        }

        return studyPlanMapper.toResponse(plan);
    }

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

    @Transactional
    public void deleteStudyPlan(UUID id, User user) {
        StudyPlan plan = studyPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Study plan not found with ID: " + id));

        if (!plan.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to delete this study plan");
        }

        studyPlanRepository.delete(plan);
    }

    @Transactional
    public StudyPlanResponse generateStudyPlan(StudyPlanGenerateRequest request, User user) {
        LocalDate effectiveStart = request.getEffectiveStartDate();
        int effectiveMinutes = request.getEffectiveMinutesPerDay();

        AiStudyPlanResponse aiResponse = aiService.generateStudyPlan(
                request.getTopic(),
                request.getDescription(),
                effectiveMinutes,
                request.getAvailableDays(),
                effectiveStart,
                user
        );

        LocalDate planStartDate = aiResponse.getStartDate() != null ? aiResponse.getStartDate() : effectiveStart;
        LocalDate planEndDate = aiResponse.getEndDate() != null ? aiResponse.getEndDate() : planStartDate.plusDays(14);

        int totalMinutes = aiResponse.getTotalEstimatedDurationMinutes();
        if (totalMinutes <= 0 && aiResponse.getTasks() != null) {
            totalMinutes = aiResponse.getTasks().stream()
                    .mapToInt(AiStudyPlanResponse.AiStudyTaskItem::getEstimatedDurationMinutes)
                    .sum();
        }

        StudyPlan plan = StudyPlan.builder()
                .title(aiResponse.getTitle() != null && !aiResponse.getTitle().isBlank() ? aiResponse.getTitle() : request.getTopic())
                .description(aiResponse.getDescription() != null ? aiResponse.getDescription() : request.getDescription())
                .category(aiResponse.getCategory() != null ? aiResponse.getCategory() : "Geral")
                .startDate(planStartDate)
                .endDate(planEndDate)
                .totalEstimatedDurationMinutes(totalMinutes)
                .availableHoursPerDay((effectiveMinutes + 59) / 60)
                .availableDays(request.getFormattedAvailableDays())
                .status(StudyPlanStatus.ACTIVE)
                .user(user)
                .build();

        StudyPlan savedPlan = studyPlanRepository.save(plan);

        if (aiResponse.getTasks() != null) {
            for (AiStudyPlanResponse.AiStudyTaskItem taskItem : aiResponse.getTasks()) {
                LocalDate taskDate = taskItem.getScheduledDate() != null ? taskItem.getScheduledDate() : planStartDate;
                StudyTask task = StudyTask.builder()
                        .title(taskItem.getTitle())
                        .description(taskItem.getDescription())
                        .sortOrder(taskItem.getOrder())
                        .estimatedDurationMinutes(taskItem.getEstimatedDurationMinutes())
                        .scheduledDate(taskDate)
                        .status(StudyTaskStatus.PENDING)
                        .studyPlan(savedPlan)
                        .build();
                StudyTask savedTask = studyTaskRepository.save(task);
                savedPlan.getStudyTasks().add(savedTask);
            }
        }

        return studyPlanMapper.toResponse(savedPlan);
    }
}
