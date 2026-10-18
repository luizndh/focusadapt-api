package com.focus.adapt.api.application.service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile;
import com.focus.adapt.api.infrastructure.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

/**
 * Valida as 20 respostas do questionário inicial e calcula o perfil de procrastinação
 * ({@link ProcrastinationProfile#PERFECTIONIST} ou {@link ProcrastinationProfile#STIMULUS_SEEKER})
 * e a intensidade correspondente (Major, Minor ou abaixo de Minor).
 */
@Component
@Slf4j
public class ProcrastinationProfileCalculator {

    public static final Set<String> PERFECTIONIST_QUESTION_IDS = Set.of(
            "P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8", "P9", "P10"
    );

    public static final Set<String> STIMULUS_SEEKER_QUESTION_IDS = Set.of(
            "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "B10"
    );

    public static final Set<String> ALL_QUESTION_IDS = Stream.concat(
            PERFECTIONIST_QUESTION_IDS.stream(),
            STIMULUS_SEEKER_QUESTION_IDS.stream()
    ).collect(Collectors.toUnmodifiableSet());

    public static final int EXPECTED_QUESTION_COUNT = 20;
    public static final int MIN_ANSWER_VALUE = 1;
    public static final int MAX_ANSWER_VALUE = 3;
    public static final int MIN_PROFILE_SCORE = 10;
    public static final int MAX_PROFILE_SCORE = 30;
    public static final int MAJOR_THRESHOLD = 25;
    public static final int MINOR_THRESHOLD = 15;

    public enum ProfileIntensity {
        MAJOR,
        MINOR,
        BELOW_MINOR
    }

    public record CalculationResult(
            ProcrastinationProfile profile,
            int perfectionistScore,
            int stimulusSeekerScore,
            ProfileIntensity perfectionistIntensity,
            ProfileIntensity stimulusSeekerIntensity,
            ProfileIntensity selectedProfileIntensity
    ) {
    }

    /**
     * Classifica a intensidade de uma pontuação conforme os limiares definidos:
     * Major (>= 25), Minor (15 a 24) ou BELOW_MINOR (< 15).
     */
    public ProfileIntensity classifyIntensity(int score) {
        if (score >= MAJOR_THRESHOLD) {
            return ProfileIntensity.MAJOR;
        }
        if (score >= MINOR_THRESHOLD) {
            return ProfileIntensity.MINOR;
        }
        return ProfileIntensity.BELOW_MINOR;
    }

    /**
     * Valida todas as 20 respostas e retorna o resultado detalhado do cálculo.
     *
     * @param answers mapa de respostas (ID da pergunta -> valor 1, 2 ou 3)
     * @return resultado contendo perfil selecionado, pontuações e intensidades
     * @throws BusinessException se o mapa for nulo, incompleto, contiver IDs duplicados/inválidos ou valores fora de 1..3
     */
    public CalculationResult calculateResult(Map<String, Integer> answers) {
        if (answers == null || answers.isEmpty()) {
            throw new BusinessException("As respostas do questionário são obrigatórias.");
        }

        Map<String, Integer> normalizedAnswers = new HashMap<>();

        for (Map.Entry<String, Integer> entry : answers.entrySet()) {
            String rawKey = entry.getKey();
            if (rawKey == null || rawKey.isBlank()) {
                throw new BusinessException("Identificador de pergunta inválido ou vazio.");
            }

            String questionId = rawKey.trim().toUpperCase(Locale.ROOT);
            if (!ALL_QUESTION_IDS.contains(questionId)) {
                throw new BusinessException("Pergunta desconhecida no questionário: " + rawKey);
            }

            if (normalizedAnswers.containsKey(questionId)) {
                throw new BusinessException("Pergunta duplicada no questionário: " + questionId);
            }

            Integer value = entry.getValue();
            if (value == null || value < MIN_ANSWER_VALUE || value > MAX_ANSWER_VALUE) {
                throw new BusinessException(
                        "Valor de resposta inválido para a pergunta " + questionId + ". Valores permitidos: 1, 2 ou 3."
                );
            }

            normalizedAnswers.put(questionId, value);
        }

        if (normalizedAnswers.size() != EXPECTED_QUESTION_COUNT
                || !normalizedAnswers.keySet().containsAll(ALL_QUESTION_IDS)) {
            throw new BusinessException(
                    "O questionário deve conter exatamente as 20 perguntas obrigatórias (P1 a P10 e B1 a B10)."
            );
        }

        int perfectionistScore = 0;
        for (String id : PERFECTIONIST_QUESTION_IDS) {
            perfectionistScore += normalizedAnswers.get(id);
        }

        int stimulusSeekerScore = 0;
        for (String id : STIMULUS_SEEKER_QUESTION_IDS) {
            stimulusSeekerScore += normalizedAnswers.get(id);
        }

        ProfileIntensity perfectionistIntensity = classifyIntensity(perfectionistScore);
        ProfileIntensity stimulusSeekerIntensity = classifyIntensity(stimulusSeekerScore);

        // Regra de decisão: maior pontuação vence; em caso de empate, seleciona PERFECTIONIST (menor risco).
        ProcrastinationProfile selectedProfile = (stimulusSeekerScore > perfectionistScore)
                ? ProcrastinationProfile.STIMULUS_SEEKER
                : ProcrastinationProfile.PERFECTIONIST;

        ProfileIntensity selectedIntensity = (selectedProfile == ProcrastinationProfile.STIMULUS_SEEKER)
                ? stimulusSeekerIntensity
                : perfectionistIntensity;

        log.info(
                "Questionnaire calculated - Perfectionist: {} ({}), Stimulus Seeker: {} ({}) -> Selected: {} ({})",
                perfectionistScore,
                perfectionistIntensity,
                stimulusSeekerScore,
                stimulusSeekerIntensity,
                selectedProfile,
                selectedIntensity
        );

        return new CalculationResult(
                selectedProfile,
                perfectionistScore,
                stimulusSeekerScore,
                perfectionistIntensity,
                stimulusSeekerIntensity,
                selectedIntensity
        );
    }

    /**
     * Valida as respostas e retorna o perfil de procrastinação calculado.
     */
    public ProcrastinationProfile calculate(Map<String, Integer> answers) {
        return calculateResult(answers).profile();
    }
}


