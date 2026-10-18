package com.focus.adapt.api.application.dto;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile;
import com.focus.adapt.api.infrastructure.exception.BusinessException;

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
     * Chave: identificador da pergunta (P1 a P10 e B1 a B10).
     * Valor: valor numérico da resposta selecionada (1, 2 ou 3).
     */
    @JsonDeserialize(using = StrictAnswersMapDeserializer.class)
    private Map<String, Integer> answers;

    /**
     * Campo legado ignorado pelo cálculo no backend (o perfil é sempre calculado a partir de answers).
     */
    private ProcrastinationProfile profile;

    /**
     * Status de conclusão do questionário.
     */
    private Boolean completed;

    public static class StrictAnswersMapDeserializer extends JsonDeserializer<Map<String, Integer>> {
        @Override
        public Map<String, Integer> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_NULL) {
                return null;
            }
            if (p.currentToken() != JsonToken.START_OBJECT) {
                throw new BusinessException("Formato inválido para o mapa de respostas do questionário.");
            }

            Map<String, Integer> result = new LinkedHashMap<>();
            Set<String> normalizedSeen = new HashSet<>();

            while (p.nextToken() != JsonToken.END_OBJECT) {
                String key = p.currentName();
                p.nextToken();

                String normalizedKey = key != null ? key.trim().toUpperCase(Locale.ROOT) : "";
                if (!normalizedSeen.add(normalizedKey)) {
                    throw new BusinessException("Pergunta duplicada no questionário: " + key);
                }

                Integer value;
                if (p.currentToken() == JsonToken.VALUE_NULL) {
                    value = null;
                } else if (p.currentToken().isNumeric()) {
                    value = p.getIntValue();
                } else {
                    throw new BusinessException("Valor de resposta inválido para a pergunta " + key);
                }

                result.put(key, value);
            }
            return result;
        }
    }
}


