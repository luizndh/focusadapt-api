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

    /**
     * Calcula o perfil de procrastinação com base nas respostas.
     *
     * @param answers mapa de respostas (chave da pergunta → valor numérico da resposta)
     * @return o perfil de procrastinação identificado
     */
    public ProcrastinationProfile calculate(Map<String, Integer> answers) {
        // TODO: implementar lógica real de cálculo baseada nas respostas do questionário
        log.info("Calculating procrastination profile from {} answers", answers != null ? answers.size() : 0);
        return ProcrastinationProfile.STIMULUS_SEEKER;
    }
}
