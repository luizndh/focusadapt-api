package com.focus.adapt.api.application.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.focus.adapt.api.application.dto.StudyTaskRequest;
import com.focus.adapt.api.application.dto.StudyTaskResponse;
import com.focus.adapt.api.application.mapper.StudyTaskMapper;
import com.focus.adapt.api.domain.entity.StudyPlan;
import com.focus.adapt.api.domain.entity.StudyTask;
import com.focus.adapt.api.infrastructure.exception.ResourceNotFoundException;
import com.focus.adapt.api.infrastructure.repository.StudyPlanRepository;
import com.focus.adapt.api.infrastructure.repository.StudyTaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudyTaskService {

    private final StudyTaskRepository studyTaskRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final StudyTaskMapper studyTaskMapper;

    @Transactional
    public StudyTaskResponse createTask(StudyTaskRequest request) {
        StudyPlan plan = studyPlanRepository.findById(request.getStudyPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("StudyPlan not found"));

        StudyTask task = studyTaskMapper.toEntity(request);
        task.setStudyPlan(plan);

        StudyTask saved = studyTaskRepository.save(task);
        return studyTaskMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StudyTaskResponse> getTasksByPlan(UUID planId) {
        StudyPlan plan = studyPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("StudyPlan not found"));
        return studyTaskRepository.findByStudyPlan(plan).stream()
                .map(studyTaskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudyTaskResponse updateTask(UUID id, StudyTaskRequest request) {
        StudyTask task = studyTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudyTask not found"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());

        StudyTask updated = studyTaskRepository.save(task);
        return studyTaskMapper.toResponse(updated);
    }

    @Transactional
    public void deleteTask(UUID id) {
        if (!studyTaskRepository.existsById(id)) {
            throw new ResourceNotFoundException("StudyTask not found");
        }
        studyTaskRepository.deleteById(id);
    }
}
