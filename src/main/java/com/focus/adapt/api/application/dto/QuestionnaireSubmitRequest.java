package com.focus.adapt.api.application.dto;

import java.util.Map;

import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireSubmitRequest {

    /**
     * Mapa de respostas do questionário.
     * Chave: identificador da pergunta (ex: "q1", "q2").
     * Valor: valor numérico da resposta selecionada (1 a 5).
     */
    private Map<String, Integer> answers;

    /**
     * Perfil calculado diretamente (opcional).
     */
    private ProcrastinationProfile profile;

    /**
     * Status de conclusão do questionário.
     */
    private Boolean completed;
}

