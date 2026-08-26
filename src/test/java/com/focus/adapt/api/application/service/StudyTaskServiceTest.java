package com.focus.adapt.api.application.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.focus.adapt.api.application.dto.AiTaskBlocksResponse;
import com.focus.adapt.api.application.dto.ContentBlockResponse;
import com.focus.adapt.api.application.dto.StudyTaskResponse;
import com.focus.adapt.api.application.mapper.ContentBlockMapper;
import com.focus.adapt.api.application.mapper.StudyTaskMapper;
import com.focus.adapt.api.domain.entity.ContentBlock;
import com.focus.adapt.api.domain.entity.StudyPlan;
import com.focus.adapt.api.domain.entity.StudyTask;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.enums.ContentBlockType;
import com.focus.adapt.api.infrastructure.exception.BusinessException;
import com.focus.adapt.api.infrastructure.repository.ContentBlockRepository;
import com.focus.adapt.api.infrastructure.repository.StudyPlanRepository;
import com.focus.adapt.api.infrastructure.repository.StudyTaskRepository;

@ExtendWith(MockitoExtension.class)
class StudyTaskServiceTest {

    @Mock
    private StudyTaskRepository studyTaskRepository;

    @Mock
    private StudyPlanRepository studyPlanRepository;

    @Mock
    private ContentBlockRepository contentBlockRepository;

    @Mock
    private StudyTaskMapper studyTaskMapper;

    @Mock
    private ContentBlockMapper contentBlockMapper;

    @Mock
    private AiService aiService;

    @InjectMocks
    private StudyTaskService studyTaskService;

    private User user;
    private StudyPlan plan;
    private StudyTask task;

    @BeforeEach
    void setUp() {
        user = User.builder().build();
        user.setId(UUID.randomUUID());

        plan = StudyPlan.builder().user(user).build();
        plan.setId(UUID.randomUUID());

        task = StudyTask.builder()
                .title("Stream Pipelines")
                .description("Understanding streams")
                .studyPlan(plan)
                .build();
        task.setId(UUID.randomUUID());
    }

    @Test
    void shouldGenerateBlocksStructureWhenBlocksDoNotExist() {
        when(studyTaskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(contentBlockRepository.findByStudyTaskOrderBySortOrderAsc(task)).thenReturn(Collections.emptyList());

        AiTaskBlocksResponse.AiBlockItem item1 = new AiTaskBlocksResponse.AiBlockItem(1, ContentBlockType.EXPLANATION, "Intro", 5);
        AiTaskBlocksResponse.AiBlockItem item2 = new AiTaskBlocksResponse.AiBlockItem(2, ContentBlockType.EXAMPLE, "Code Example", 5);
        AiTaskBlocksResponse aiResponse = new AiTaskBlocksResponse(List.of(item1, item2));

        when(aiService.generateTaskBlocksStructure(task, user)).thenReturn(aiResponse);
        when(contentBlockRepository.save(any(ContentBlock.class))).thenAnswer(i -> i.getArgument(0));

        StudyTaskResponse expectedResponse = StudyTaskResponse.builder().id(task.getId()).build();
        when(studyTaskMapper.toResponse(any(StudyTask.class))).thenReturn(expectedResponse);

        StudyTaskResponse response = studyTaskService.getTaskWithBlocks(task.getId(), user);

        assertNotNull(response);
        verify(aiService).generateTaskBlocksStructure(task, user);
    }

    @Test
    void shouldNotCallAiWhenBlocksAlreadyExist() {
        ContentBlock existingBlock = ContentBlock.builder()
                .sortOrder(1)
                .blockType(ContentBlockType.EXPLANATION)
                .title("Intro")
                .studyTask(task)
                .build();

        when(studyTaskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(contentBlockRepository.findByStudyTaskOrderBySortOrderAsc(task)).thenReturn(List.of(existingBlock));

        StudyTaskResponse expectedResponse = StudyTaskResponse.builder().id(task.getId()).build();
        when(studyTaskMapper.toResponse(any(StudyTask.class))).thenReturn(expectedResponse);

        studyTaskService.getTaskWithBlocks(task.getId(), user);

        verify(aiService, never()).generateTaskBlocksStructure(any(), any());
    }

    @Test
    void shouldLazilyGenerateBlockContentWhenEmpty() {
        UUID blockId = UUID.randomUUID();
        ContentBlock block = ContentBlock.builder()
                .sortOrder(1)
                .blockType(ContentBlockType.EXPLANATION)
                .title("Intro")
                .content(null)
                .studyTask(task)
                .build();
        block.setId(blockId);

        when(studyTaskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(contentBlockRepository.findById(blockId)).thenReturn(Optional.of(block));
        when(aiService.generateBlockContent(task, block, user)).thenReturn("# Generated Markdown Content");
        when(contentBlockRepository.save(any(ContentBlock.class))).thenReturn(block);

        ContentBlockResponse expectedResponse = ContentBlockResponse.builder()
                .id(blockId)
                .content("# Generated Markdown Content")
                .build();
        when(contentBlockMapper.toResponse(any(ContentBlock.class))).thenReturn(expectedResponse);

        ContentBlockResponse response = studyTaskService.getBlockContent(task.getId(), blockId, user);

        assertNotNull(response);
        verify(aiService).generateBlockContent(task, block, user);
        verify(contentBlockRepository).save(block);
    }

    @Test
    void shouldReturnCachedContentWhenAlreadyPresent() {
        UUID blockId = UUID.randomUUID();
        ContentBlock block = ContentBlock.builder()
                .sortOrder(1)
                .blockType(ContentBlockType.EXPLANATION)
                .title("Intro")
                .content("# Already Cached Content")
                .studyTask(task)
                .build();
        block.setId(blockId);

        when(studyTaskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(contentBlockRepository.findById(blockId)).thenReturn(Optional.of(block));

        ContentBlockResponse expectedResponse = ContentBlockResponse.builder()
                .id(blockId)
                .content("# Already Cached Content")
                .build();
        when(contentBlockMapper.toResponse(block)).thenReturn(expectedResponse);

        ContentBlockResponse response = studyTaskService.getBlockContent(task.getId(), blockId, user);

        assertNotNull(response);
        verify(aiService, never()).generateBlockContent(any(), any(), any());
        verify(contentBlockRepository, never()).save(any());
    }

    @Test
    void shouldUpdateBlockCompletion() {
        UUID blockId = UUID.randomUUID();
        ContentBlock block = ContentBlock.builder()
                .sortOrder(1)
                .blockType(ContentBlockType.QUIZ)
                .title("Quiz 1")
                .completed(false)
                .studyTask(task)
                .build();
        block.setId(blockId);

        when(studyTaskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(contentBlockRepository.findById(blockId)).thenReturn(Optional.of(block));
        when(contentBlockRepository.save(block)).thenReturn(block);

        ContentBlockResponse expectedResponse = ContentBlockResponse.builder()
                .id(blockId)
                .completed(true)
                .build();
        when(contentBlockMapper.toResponse(block)).thenReturn(expectedResponse);

        ContentBlockResponse response = studyTaskService.updateBlockCompletion(task.getId(), blockId, true, user);

        assertNotNull(response);
        assertTrue(response.isCompleted());
        verify(contentBlockRepository).save(block);
    }

    @Test
    void shouldRejectAccessForUnownedTask() {
        User otherUser = User.builder().build();
        otherUser.setId(UUID.randomUUID());

        when(studyTaskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThrows(BusinessException.class, () -> studyTaskService.getTaskWithBlocks(task.getId(), otherUser));
    }
}
