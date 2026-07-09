package com.focus.adapt.api.application.mapper;

import com.focus.adapt.api.application.dto.AnswerOptionRequest;
import com.focus.adapt.api.application.dto.AnswerOptionResponse;
import com.focus.adapt.api.application.dto.ProcrastinationProfileResponse;
import com.focus.adapt.api.application.dto.QuestionRequest;
import com.focus.adapt.api.application.dto.QuestionResponse;
import com.focus.adapt.api.application.dto.QuestionnaireRequest;
import com.focus.adapt.api.application.dto.QuestionnaireResponse;
import com.focus.adapt.api.domain.entity.AnswerOption;
import com.focus.adapt.api.domain.entity.ProcrastinationProfile;
import com.focus.adapt.api.domain.entity.Question;
import com.focus.adapt.api.domain.entity.Questionnaire;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface QuestionnaireMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Questionnaire toEntity(QuestionnaireRequest request);

    QuestionnaireResponse toResponse(Questionnaire entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "questionnaire", ignore = true)
    Question toEntity(QuestionRequest request);

    QuestionResponse toResponse(Question entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "question", ignore = true)
    AnswerOption toEntity(AnswerOptionRequest request);

    AnswerOptionResponse toResponse(AnswerOption entity);

    ProcrastinationProfileResponse toResponse(ProcrastinationProfile entity);
}
