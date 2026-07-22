package com.focus.adapt.api.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.focus.adapt.api.application.dto.ProcrastinationProfileResponse;
import com.focus.adapt.api.application.dto.UserAnswerItemRequest;
import com.focus.adapt.api.application.dto.UserAnswerSubmitRequest;
import com.focus.adapt.api.application.mapper.QuestionnaireMapper;
import com.focus.adapt.api.domain.entity.AnswerOption;
import com.focus.adapt.api.domain.entity.ProcrastinationProfile;
import com.focus.adapt.api.domain.entity.Question;
import com.focus.adapt.api.domain.entity.Questionnaire;
import com.focus.adapt.api.domain.entity.User;
import com.focus.adapt.api.domain.entity.UserAnswer;
import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfileType;
import com.focus.adapt.api.infrastructure.repository.AnswerOptionRepository;
import com.focus.adapt.api.infrastructure.repository.ProcrastinationProfileRepository;
import com.focus.adapt.api.infrastructure.repository.QuestionRepository;
import com.focus.adapt.api.infrastructure.repository.QuestionnaireRepository;
import com.focus.adapt.api.infrastructure.repository.UserAnswerRepository;
import com.focus.adapt.api.infrastructure.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class QuestionnaireServiceTest {

    @Mock
    private QuestionnaireRepository questionnaireRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private AnswerOptionRepository answerOptionRepository;
    @Mock
    private UserAnswerRepository userAnswerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProcrastinationProfileRepository procrastinationProfileRepository;
    @Mock
    private QuestionnaireMapper questionnaireMapper;
    @Mock
    private GeminiService geminiService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private QuestionnaireService questionnaireService;

    @Test
    void shouldSubmitAnswersAndIdentifyProfile() {
        UUID qId = UUID.randomUUID();
        User user = User.builder().name("John").points(0).build();

        Questionnaire questionnaire = Questionnaire.builder().active(true).build();
        when(questionnaireRepository.findById(qId)).thenReturn(Optional.of(questionnaire));

        UUID questionId = UUID.randomUUID();
        Question question = Question.builder().build();
        question.setId(questionId);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        UUID optionId = UUID.randomUUID();
        AnswerOption option = AnswerOption.builder()
                .procrastinationProfileType(ProcrastinationProfileType.PERFECTIONIST)
                .value(5)
                .question(question)
                .build();
        when(answerOptionRepository.findById(optionId)).thenReturn(Optional.of(option));

        UserAnswerSubmitRequest submitRequest = UserAnswerSubmitRequest.builder()
                .goals("Finish thesis")
                .answers(List.of(UserAnswerItemRequest.builder()
                        .questionId(questionId)
                        .selectedOptionId(optionId)
                        .build()))
                .build();

        ProcrastinationProfile profile = ProcrastinationProfile.builder()
                .profileType(ProcrastinationProfileType.PERFECTIONIST)
                .build();
        when(procrastinationProfileRepository.findByProfileType(ProcrastinationProfileType.PERFECTIONIST))
                .thenReturn(Optional.of(profile));

        when(userRepository.save(any(User.class))).thenReturn(user);

        ProcrastinationProfileResponse responseDto = ProcrastinationProfileResponse.builder()
                .profileType(ProcrastinationProfileType.PERFECTIONIST)
                .build();
        when(questionnaireMapper.toResponse(profile)).thenReturn(responseDto);

        ProcrastinationProfileResponse response = questionnaireService.submitAnswers(qId, submitRequest, user);

        assertNotNull(response);
        assertEquals(ProcrastinationProfileType.PERFECTIONIST, response.getProfileType());

        // Verify calculations and saving
        verify(userAnswerRepository, times(1)).save(any(UserAnswer.class));
        verify(userRepository, times(1)).save(user);

        // Verify event published
        verify(eventPublisher, times(1)).publishEvent(any(com.focus.adapt.api.application.event.QuestionnaireCompletedEvent.class));
    }
}
