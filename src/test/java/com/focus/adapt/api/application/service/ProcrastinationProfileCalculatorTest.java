package com.focus.adapt.api.application.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.focus.adapt.api.application.service.ProcrastinationProfileCalculator.CalculationResult;
import com.focus.adapt.api.application.service.ProcrastinationProfileCalculator.ProfileIntensity;
import com.focus.adapt.api.domain.entity.enums.ProcrastinationProfile;
import com.focus.adapt.api.infrastructure.exception.BusinessException;

class ProcrastinationProfileCalculatorTest {

    private ProcrastinationProfileCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ProcrastinationProfileCalculator();
    }

    private Map<String, Integer> buildAnswers(int defaultPerfectionist, int defaultStimulusSeeker) {
        Map<String, Integer> answers = new LinkedHashMap<>();
        for (int i = 1; i <= 10; i++) {
            answers.put("P" + i, defaultPerfectionist);
            answers.put("B" + i, defaultStimulusSeeker);
        }
        return answers;
    }

    @Test
    void shouldCalculatePerfectionistMajorAndMinMaxScoresCorrectly() {
        Map<String, Integer> answers = buildAnswers(3, 1);

        CalculationResult result = calculator.calculateResult(answers);

        assertEquals(30, result.perfectionistScore());
        assertEquals(10, result.stimulusSeekerScore());
        assertEquals(ProfileIntensity.MAJOR, result.perfectionistIntensity());
        assertEquals(ProfileIntensity.BELOW_MINOR, result.stimulusSeekerIntensity());
        assertEquals(ProcrastinationProfile.PERFECTIONIST, result.profile());
        assertEquals(ProfileIntensity.MAJOR, result.selectedProfileIntensity());
    }

    @Test
    void shouldCalculateStimulusSeekerMajorWhenStimulusSeekerScoreIsHigher() {
        Map<String, Integer> answers = buildAnswers(1, 3);

        CalculationResult result = calculator.calculateResult(answers);

        assertEquals(10, result.perfectionistScore());
        assertEquals(30, result.stimulusSeekerScore());
        assertEquals(ProfileIntensity.BELOW_MINOR, result.perfectionistIntensity());
        assertEquals(ProfileIntensity.MAJOR, result.stimulusSeekerIntensity());
        assertEquals(ProcrastinationProfile.STIMULUS_SEEKER, result.profile());
        assertEquals(ProfileIntensity.MAJOR, result.selectedProfileIntensity());
    }

    @Test
    void shouldSelectPerfectionistOnTieAcrossAllThresholdBands() {
        // Empate em Major (25 vs 25)
        Map<String, Integer> majorTie = buildAnswers(2, 2);
        for (int i = 1; i <= 5; i++) {
            majorTie.put("P" + i, 3);
            majorTie.put("B" + i, 3);
        }
        CalculationResult majorResult = calculator.calculateResult(majorTie);
        assertEquals(25, majorResult.perfectionistScore());
        assertEquals(25, majorResult.stimulusSeekerScore());
        assertEquals(ProcrastinationProfile.PERFECTIONIST, majorResult.profile());
        assertEquals(ProfileIntensity.MAJOR, majorResult.selectedProfileIntensity());

        // Empate em Minor (20 vs 20)
        Map<String, Integer> minorTie = buildAnswers(2, 2);
        CalculationResult minorResult = calculator.calculateResult(minorTie);
        assertEquals(20, minorResult.perfectionistScore());
        assertEquals(20, minorResult.stimulusSeekerScore());
        assertEquals(ProcrastinationProfile.PERFECTIONIST, minorResult.profile());
        assertEquals(ProfileIntensity.MINOR, minorResult.selectedProfileIntensity());

        // Empate abaixo de Minor (10 vs 10)
        Map<String, Integer> belowMinorTie = buildAnswers(1, 1);
        CalculationResult belowMinorResult = calculator.calculateResult(belowMinorTie);
        assertEquals(10, belowMinorResult.perfectionistScore());
        assertEquals(10, belowMinorResult.stimulusSeekerScore());
        assertEquals(ProcrastinationProfile.PERFECTIONIST, belowMinorResult.profile());
        assertEquals(ProfileIntensity.BELOW_MINOR, belowMinorResult.selectedProfileIntensity());
    }

    @Test
    void shouldClassifyMajorMinorAndBelowMinorThresholdsCorrectly() {
        assertEquals(ProfileIntensity.BELOW_MINOR, calculator.classifyIntensity(10));
        assertEquals(ProfileIntensity.BELOW_MINOR, calculator.classifyIntensity(14));
        assertEquals(ProfileIntensity.MINOR, calculator.classifyIntensity(15));
        assertEquals(ProfileIntensity.MINOR, calculator.classifyIntensity(24));
        assertEquals(ProfileIntensity.MAJOR, calculator.classifyIntensity(25));
        assertEquals(ProfileIntensity.MAJOR, calculator.classifyIntensity(30));

        // Ambos >= 25 (26 vs 28 -> STIMULUS_SEEKER MAJOR)
        Map<String, Integer> bothMajor = buildAnswers(2, 2);
        for (int i = 1; i <= 6; i++) {
            bothMajor.put("P" + i, 3);
        }
        for (int i = 1; i <= 8; i++) {
            bothMajor.put("B" + i, 3);
        }
        CalculationResult bothMajorResult = calculator.calculateResult(bothMajor);
        assertEquals(26, bothMajorResult.perfectionistScore());
        assertEquals(28, bothMajorResult.stimulusSeekerScore());
        assertEquals(ProcrastinationProfile.STIMULUS_SEEKER, bothMajorResult.profile());
        assertEquals(ProfileIntensity.MAJOR, bothMajorResult.selectedProfileIntensity());

        // Ambos < 25 (15 vs 24 -> STIMULUS_SEEKER MINOR)
        Map<String, Integer> bothMinor = buildAnswers(1, 2);
        for (int i = 1; i <= 5; i++) {
            bothMinor.put("P" + i, 2);
        }
        for (int i = 1; i <= 4; i++) {
            bothMinor.put("B" + i, 3);
        }
        CalculationResult bothMinorResult = calculator.calculateResult(bothMinor);
        assertEquals(15, bothMinorResult.perfectionistScore());
        assertEquals(24, bothMinorResult.stimulusSeekerScore());
        assertEquals(ProcrastinationProfile.STIMULUS_SEEKER, bothMinorResult.profile());
        assertEquals(ProfileIntensity.MINOR, bothMinorResult.selectedProfileIntensity());
    }

    @Test
    void shouldRejectNullEmptyIncompleteDuplicateOrInvalidAnswers() {
        assertThrows(BusinessException.class, () -> calculator.calculate(null));
        assertThrows(BusinessException.class, () -> calculator.calculate(Map.of()));

        // Resposta faltando (19 perguntas)
        Map<String, Integer> missingOne = buildAnswers(2, 2);
        missingOne.remove("B10");
        assertThrows(BusinessException.class, () -> calculator.calculate(missingOne));

        // ID desconhecido
        Map<String, Integer> unknownId = buildAnswers(2, 2);
        unknownId.remove("P10");
        unknownId.put("P11", 2);
        assertThrows(BusinessException.class, () -> calculator.calculate(unknownId));

        // Pergunta duplicada com caixa diferente ("P1" e "p1")
        Map<String, Integer> duplicateCaseId = new HashMap<>(buildAnswers(2, 2));
        duplicateCaseId.put("p1", 3);
        assertThrows(BusinessException.class, () -> calculator.calculate(duplicateCaseId));

        // Valor menor que 1
        Map<String, Integer> valueTooLow = buildAnswers(2, 2);
        valueTooLow.put("P1", 0);
        assertThrows(BusinessException.class, () -> calculator.calculate(valueTooLow));

        // Valor maior que 3
        Map<String, Integer> valueTooHigh = buildAnswers(2, 2);
        valueTooHigh.put("B1", 4);
        assertThrows(BusinessException.class, () -> calculator.calculate(valueTooHigh));

        // Valor nulo
        Map<String, Integer> nullValue = new HashMap<>(buildAnswers(2, 2));
        nullValue.put("P5", null);
        assertThrows(BusinessException.class, () -> calculator.calculate(nullValue));
    }
}

