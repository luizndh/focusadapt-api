package com.focus.adapt.api.application.service;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile;

import lombok.extern.slf4j.Slf4j;

/**
 * Calcula o perfil de procrastinação do usuário com base nas respostas do questionário.
 */
@Component
@Slf4j
public class ProcrastinationProfileCalculator {

    // Questões do perfil Perfeccionista e seus respectivos pesos
    private static final Map<String, Double> PERFECTIONIST_WEIGHTS = Map.of(
            "q2", 1.2,
            "q4", 1.3,
            "q6", 1.1,
            "q8", 1.2,
            "q10", 1.2,
            "q12", 1.3,
            "q14", 1.1,
            "q16", 1.2,
            "q18", 1.1,
            "q20", 1.2
    );

    // Questões do perfil Buscador de Estímulos e seus respectivos pesos
    private static final Map<String, Double> STIMULUS_SEEKER_WEIGHTS = Map.of(
            "q1", 1.0,
            "q3", 1.1,
            "q5", 1.0,
            "q7", 1.2,
            "q9", 1.1,
            "q11", 1.0,
            "q13", 1.0,
            "q15", 1.0,
            "q17", 1.0,
            "q19", 1.1
    );

    /**
     * Calcula o perfil de procrastinação com base nas respostas.
     *
     * @param answers mapa de respostas (chave da pergunta → valor numérico da resposta de 1 a 5)
     * @return o perfil de procrastinação identificado
     */
    public ProcrastinationProfile calculate(Map<String, Integer> answers) {
        if (answers == null || answers.isEmpty()) {
            log.warn("Answers map is null or empty, defaulting to PERFECTIONIST");
            return ProcrastinationProfile.PERFECTIONIST;
        }

        double perfectionistScore = 0.0;
        double stimulusSeekerScore = 0.0;
        double perfectionistWeighted = 0.0;
        double stimulusSeekerWeighted = 0.0;

        for (Map.Entry<String, Integer> entry : answers.entrySet()) {
            String questionId = entry.getKey().toLowerCase();
            int score = entry.getValue() != null ? entry.getValue() : 0;

            if (PERFECTIONIST_WEIGHTS.containsKey(questionId)) {
                double weight = PERFECTIONIST_WEIGHTS.get(questionId);
                perfectionistScore += score;
                perfectionistWeighted += score * weight;
            } else if (STIMULUS_SEEKER_WEIGHTS.containsKey(questionId)) {
                double weight = STIMULUS_SEEKER_WEIGHTS.get(questionId);
                stimulusSeekerScore += score;
                stimulusSeekerWeighted += score * weight;
            }
        }

        log.info("Calculated scores - Perfectionist: {} (weighted: {}), Stimulus Seeker: {} (weighted: {})",
                perfectionistScore, perfectionistWeighted, stimulusSeekerScore, stimulusSeekerWeighted);

        if (perfectionistScore > stimulusSeekerScore) {
            return ProcrastinationProfile.PERFECTIONIST;
        } else if (stimulusSeekerScore > perfectionistScore) {
            return ProcrastinationProfile.STIMULUS_SEEKER;
        } else {
            // Desempate pelo score ponderado
            return perfectionistWeighted >= stimulusSeekerWeighted
                    ? ProcrastinationProfile.PERFECTIONIST
                    : ProcrastinationProfile.STIMULUS_SEEKER;
        }
    }
}

