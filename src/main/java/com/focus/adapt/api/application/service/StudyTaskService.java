package com.focus.adapt.api.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.focus.adapt.api.application.dto.AiTaskBlocksResponse;
import com.focus.adapt.api.application.dto.ContentBlockResponse;
import com.focus.adapt.api.application.dto.StudyTaskRequest;
import com.focus.adapt.api.application.dto.StudyTaskResponse;
import com.focus.adapt.api.application.mapper.ContentBlockMapper;
import com.focus.adapt.api.application.mapper.StudyTaskMapper;
import com.focus.adapt.api.domain.entity.ContentBlock;
import com.focus.adapt.api.domain.entity.StudyPlan;
import com.focus.adapt.api.domain.entity.StudyTask;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.infrastructure.exception.BusinessException;
import com.focus.adapt.api.infrastructure.exception.ResourceNotFoundException;
import com.focus.adapt.api.infrastructure.repository.ContentBlockRepository;
import com.focus.adapt.api.infrastructure.repository.StudyPlanRepository;
import com.focus.adapt.api.infrastructure.repository.StudyTaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudyTaskService {

    private final StudyTaskRepository studyTaskRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final ContentBlockRepository contentBlockRepository;
    private final StudyTaskMapper studyTaskMapper;
    private final ContentBlockMapper contentBlockMapper;
    private final AiService aiService;

    private final ConcurrentHashMap<UUID, CompletableFuture<String>> inFlightGenerations = new ConcurrentHashMap<>();

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

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

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

    /**
     * ETAPA 1: Obtém a StudyTask com a lista de ContentBlocks.
     * Se a estrutura ainda não existir, chama o Gemini para planejar os blocos e salva no banco.
     */
    @Transactional
    public StudyTaskResponse getTaskWithBlocks(UUID taskId, User user) {
        StudyTask task = getTaskAndValidateOwnership(taskId, user);

        List<ContentBlock> existingBlocks = contentBlockRepository.findByStudyTaskOrderBySortOrderAsc(task);

        if (existingBlocks.isEmpty()) {
            AiTaskBlocksResponse aiBlocks = aiService.generateTaskBlocksStructure(task, user);

            if (aiBlocks.getBlocks() != null && !aiBlocks.getBlocks().isEmpty()) {
                task.getContentBlocks().clear();
                for (AiTaskBlocksResponse.AiBlockItem item : aiBlocks.getBlocks()) {
                    ContentBlock block = ContentBlock.builder()
                            .sortOrder(item.getOrder())
                            .blockType(item.getType())
                            .title(item.getTitle())
                            .estimatedMinutes(item.getEstimatedMinutes() > 0 ? item.getEstimatedMinutes() : 5)
                            .completed(false)
                            .studyTask(task)
                            .build();
                    ContentBlock saved = contentBlockRepository.save(block);
                    task.getContentBlocks().add(saved);
                }
            }
        } else {
            if (task.getContentBlocks() != existingBlocks) {
                task.getContentBlocks().clear();
                task.getContentBlocks().addAll(existingBlocks);
            }
        }

        return studyTaskMapper.toResponseWithBlocks(task);
    }

    /**
     * ETAPA 2: Obtém o conteúdo de um ContentBlock específico sob demanda (Lazy Generation).
     * Se o conteúdo já existir no banco de dados, retorna diretamente sem chamar a IA.
     */
    @Transactional
    public ContentBlockResponse getBlockContent(UUID taskId, UUID blockId, User user) {
        StudyTask task = getTaskAndValidateOwnership(taskId, user);

        ContentBlock block = contentBlockRepository.findById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("ContentBlock not found with ID: " + blockId));

        if (!block.getStudyTask().getId().equals(task.getId())) {
            throw new BusinessException("ContentBlock does not belong to the specified StudyTask");
        }

        if (block.getContent() != null && !block.getContent().isBlank()) {
            return contentBlockMapper.toResponse(block);
        }

        CompletableFuture<String> newFuture = new CompletableFuture<>();
        CompletableFuture<String> existingFuture = inFlightGenerations.putIfAbsent(blockId, newFuture);

        if (existingFuture != null) {
            try {
                String generatedContent = existingFuture.join();
                block.setContent(generatedContent);
                return contentBlockMapper.toResponse(block);
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new BusinessException("Erro ao gerar conteúdo do bloco: " + cause.getMessage());
            }
        }

        try {
            String content = aiService.generateBlockContent(task, block, user);
            block.setContent(content);
            block = contentBlockRepository.save(block);
            newFuture.complete(content);
            return contentBlockMapper.toResponse(block);
        } catch (Throwable t) {
            newFuture.completeExceptionally(t);
            if (t instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new BusinessException("Erro ao gerar conteúdo do bloco: " + t.getMessage());
        } finally {
            inFlightGenerations.remove(blockId);
        }
    }

    /**
     * Marca ou desmarca um ContentBlock como concluído.
     */
    @Transactional
    public ContentBlockResponse updateBlockCompletion(UUID taskId, UUID blockId, boolean completed, User user) {
        StudyTask task = getTaskAndValidateOwnership(taskId, user);

        ContentBlock block = contentBlockRepository.findById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("ContentBlock not found with ID: " + blockId));

        if (!block.getStudyTask().getId().equals(task.getId())) {
            throw new BusinessException("ContentBlock does not belong to the specified StudyTask");
        }

        block.setCompleted(completed);
        ContentBlock updated = contentBlockRepository.save(block);
        return contentBlockMapper.toResponse(updated);
    }

    /**
     * Endpoint legado mantido para compatibilidade.
     */
    @Transactional
    public StudyTaskResponse getTaskWithContent(UUID taskId, User user) {
        return getTaskWithBlocks(taskId, user);
    }

    private StudyTask getTaskAndValidateOwnership(UUID taskId, User user) {
        StudyTask task = studyTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("StudyTask not found with ID: " + taskId));

        if (!task.getStudyPlan().getUser().getId().equals(user.getId())) {
            throw new BusinessException("You do not have permission to access this StudyTask");
        }

        return task;
    }
}
