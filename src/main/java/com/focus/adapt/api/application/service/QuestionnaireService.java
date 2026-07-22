package com.focus.adapt.api.application.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.focus.adapt.api.application.dto.ProcrastinationProfileResponse;
import com.focus.adapt.api.application.dto.QuestionnaireRequest;
import com.focus.adapt.api.application.dto.QuestionnaireResponse;
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
import com.focus.adapt.api.infrastructure.exception.BusinessException;
import com.focus.adapt.api.infrastructure.exception.ResourceNotFoundException;
import com.focus.adapt.api.infrastructure.repository.AnswerOptionRepository;
import com.focus.adapt.api.infrastructure.repository.ProcrastinationProfileRepository;
import com.focus.adapt.api.infrastructure.repository.QuestionRepository;
import com.focus.adapt.api.infrastructure.repository.QuestionnaireRepository;
import com.focus.adapt.api.infrastructure.repository.UserAnswerRepository;
import com.focus.adapt.api.infrastructure.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final UserRepository userRepository;
    private final ProcrastinationProfileRepository procrastinationProfileRepository;
    private final QuestionnaireMapper questionnaireMapper;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Transactional
    public QuestionnaireResponse createQuestionnaire(QuestionnaireRequest request) {
        Questionnaire questionnaire = questionnaireMapper.toEntity(request);

        // Configure bidirectional relationships so cascade saves work correctly
        if (questionnaire.getQuestions() != null) {
            questionnaire.getQuestions().forEach(q -> {
                q.setQuestionnaire(questionnaire);
                if (q.getOptions() != null) {
                    q.getOptions().forEach(o -> o.setQuestion(q));
                }
            });
        }

        Questionnaire saved = questionnaireRepository.save(questionnaire);
        return questionnaireMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<QuestionnaireResponse> getQuestionnaires(Boolean activeOnly, Pageable pageable) {
        Page<Questionnaire> questionnaires;
        if (activeOnly != null && activeOnly) {
            questionnaires = questionnaireRepository.findByActive(true, pageable);
        } else {
            questionnaires = questionnaireRepository.findAll(pageable);
        }
        return questionnaires.map(questionnaireMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public QuestionnaireResponse getQuestionnaireById(UUID id) {
        Questionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questionnaire not found with ID: " + id));
        return questionnaireMapper.toResponse(questionnaire);
    }

    @Transactional
    public QuestionnaireResponse updateQuestionnaire(UUID id, QuestionnaireRequest request) {
        Questionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questionnaire not found with ID: " + id));

        questionnaire.setTitle(request.getTitle());
        questionnaire.setDescription(request.getDescription());
        questionnaire.setActive(request.isActive());

        // For simplicity, we update basic info. Complex nested question updates should be done via migration or aggregate mapping.
        Questionnaire updated = questionnaireRepository.save(questionnaire);
        return questionnaireMapper.toResponse(updated);
    }

    @Transactional
    public void deleteQuestionnaire(UUID id) {
        if (!questionnaireRepository.existsById(id)) {
            throw new ResourceNotFoundException("Questionnaire not found with ID: " + id);
        }
        questionnaireRepository.deleteById(id);
    }

    @Transactional
    public ProcrastinationProfileResponse submitAnswers(UUID questionnaireId, UserAnswerSubmitRequest request, User user) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new ResourceNotFoundException("Questionnaire not found with ID: " + questionnaireId));

        if (!questionnaire.isActive()) {
            throw new BusinessException("Cannot submit answers to an inactive questionnaire");
        }

        // Tally answers to decide the dominant profile
        Map<ProcrastinationProfileType, Integer> profileTally = new HashMap<>();

        for (UserAnswerItemRequest item : request.getAnswers()) {
            Question question = questionRepository.findById(item.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + item.getQuestionId()));

            AnswerOption option = answerOptionRepository.findById(item.getSelectedOptionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Answer Option not found with ID: " + item.getSelectedOptionId()));

            if (!option.getQuestion().getId().equals(question.getId())) {
                throw new BusinessException("Answer option does not belong to the specified question");
            }

            // Save the user's answer history
            UserAnswer userAnswer = UserAnswer.builder()
                    .user(user)
                    .question(question)
                    .selectedOption(option)
                    .build();
            userAnswerRepository.save(userAnswer);

            // Tally the score
            if (option.getProcrastinationProfileType() != null) {
                profileTally.put(
                        option.getProcrastinationProfileType(),
                        profileTally.getOrDefault(option.getProcrastinationProfileType(), 0) + option.getValue()
                );
            }
        }

        // Determine the dominant profile type (highest accumulated points)
        ProcrastinationProfileType dominantType = profileTally.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ProcrastinationProfileType.STIMULUS_SEEKER); // Default fallback

        // Find or create the ProcrastinationProfile config
        ProcrastinationProfile profile = procrastinationProfileRepository.findByProfileType(dominantType)
                .orElseGet(() -> {
                    // Fallback create if DB configuration is not pre-populated
                    ProcrastinationProfile newProfile = ProcrastinationProfile.builder()
                            .profileType(dominantType)
                            .description("Default description for " + dominantType.name())
                            .characteristics("Default characteristics")
                            .tips("Default adaptive tips for focus")
                            .build();
                    return procrastinationProfileRepository.save(newProfile);
                });

        // Link the profile to the current user
        user.setProcrastinationProfile(profile);
        User savedUser = userRepository.save(user);

        // Publish event for async processing (Gemini integration, study plans, etc)
        eventPublisher.publishEvent(new com.focus.adapt.api.application.event.QuestionnaireCompletedEvent(savedUser, request.getGoals()));

        return questionnaireMapper.toResponse(profile);
    }
}
