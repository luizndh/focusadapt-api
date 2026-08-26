package com.focus.adapt.api.application.service;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile;

class ProcrastinationProfileCalculatorTest {

    private ProcrastinationProfileCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ProcrastinationProfileCalculator();
    }

    @Test
    void shouldCalculatePerfectionistWhenPerfectionistAnswersAreHigher() {
        Map<String, Integer> answers = new HashMap<>();
        // Perguntas perfeccionistas (pares) com nota máxima (5)
        answers.put("q2", 5);
        answers.put("q4", 5);
        answers.put("q6", 5);
        answers.put("q8", 5);
        answers.put("q10", 5);
        answers.put("q12", 5);
        answers.put("q14", 5);
        answers.put("q16", 5);
        answers.put("q18", 5);
        answers.put("q20", 5);

        // Perguntas buscador de estímulos (ímpares) com nota mínima (1)
        answers.put("q1", 1);
        answers.put("q3", 1);
        answers.put("q5", 1);
        answers.put("q7", 1);
        answers.put("q9", 1);
        answers.put("q11", 1);
        answers.put("q13", 1);
        answers.put("q15", 1);
        answers.put("q17", 1);
        answers.put("q19", 1);

        ProcrastinationProfile profile = calculator.calculate(answers);
        assertEquals(ProcrastinationProfile.PERFECTIONIST, profile);
    }

    @Test
    void shouldCalculateStimulusSeekerWhenStimulusSeekerAnswersAreHigher() {
        Map<String, Integer> answers = new HashMap<>();
        // Perguntas perfeccionistas (pares) com nota mínima (1)
        answers.put("q2", 1);
        answers.put("q4", 1);
        answers.put("q6", 1);
        answers.put("q8", 1);
        answers.put("q10", 1);
        answers.put("q12", 1);
        answers.put("q14", 1);
        answers.put("q16", 1);
        answers.put("q18", 1);
        answers.put("q20", 1);

        // Perguntas buscador de estímulos (ímpares) com nota máxima (5)
        answers.put("q1", 5);
        answers.put("q3", 5);
        answers.put("q5", 5);
        answers.put("q7", 5);
        answers.put("q9", 5);
        answers.put("q11", 5);
        answers.put("q13", 5);
        answers.put("q15", 5);
        answers.put("q17", 5);
        answers.put("q19", 5);

        ProcrastinationProfile profile = calculator.calculate(answers);
        assertEquals(ProcrastinationProfile.STIMULUS_SEEKER, profile);
    }

    @Test
    void shouldHandleNullOrEmptyAnswersGracefully() {
        assertEquals(ProcrastinationProfile.PERFECTIONIST, calculator.calculate(null));
        assertEquals(ProcrastinationProfile.PERFECTIONIST, calculator.calculate(Map.of()));
    }
}
