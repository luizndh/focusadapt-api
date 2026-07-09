package com.focus.adapt.api.application.service;

import com.focus.adapt.api.application.dto.ProcrastinationProfileResponse;
import com.focus.adapt.api.application.dto.QuestionnaireRequest;
import com.focus.adapt.api.application.dto.QuestionnaireResponse;
import com.focus.adapt.api.application.dto.UserAnswerSubmitRequest;
import com.focus.adapt.api.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface QuestionnaireService {
    QuestionnaireResponse createQuestionnaire(QuestionnaireRequest request);
    Page<QuestionnaireResponse> getQuestionnaires(Boolean activeOnly, Pageable pageable);
    QuestionnaireResponse getQuestionnaireById(UUID id);
    QuestionnaireResponse updateQuestionnaire(UUID id, QuestionnaireRequest request);
    void deleteQuestionnaire(UUID id);
    ProcrastinationProfileResponse submitAnswers(UUID questionnaireId, UserAnswerSubmitRequest request, User user);
}
