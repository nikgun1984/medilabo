package com.medilabo.assessment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TriggerTerms utility.
 */
class TriggerTermsTest {

    @Test
    void findTriggers_nullText_returnsEmpty() {
        assertEquals(List.of(), TriggerTerms.findTriggers(null));
    }

    @Test
    void findTriggers_blankText_returnsEmpty() {
        assertEquals(List.of(), TriggerTerms.findTriggers("   "));
    }

    @Test
    void findTriggers_noMatches_returnsEmpty() {
        assertEquals(List.of(), TriggerTerms.findTriggers("The patient is doing fine."));
    }

    @Test
    void findTriggers_singleMatch() {
        List<String> result = TriggerTerms.findTriggers("Patient shows abnormal blood pressure.");
        assertEquals(List.of("Abnormal"), result);
    }

    @Test
    void findTriggers_multipleMatches() {
        String text = "Hemoglobin A1C elevated. Weight above average. Cholesterol high.";
        List<String> result = TriggerTerms.findTriggers(text);
        assertTrue(result.contains("Hemoglobin A1C"));
        assertTrue(result.contains("Weight"));
        assertTrue(result.contains("Cholesterol"));
        assertEquals(3, result.size());
    }

    @Test
    void findTriggers_caseInsensitive() {
        String text = "HEMOGLOBIN A1C is fine. ABNORMAL readings noted.";
        List<String> result = TriggerTerms.findTriggers(text);
        assertTrue(result.contains("Hemoglobin A1C"));
        assertTrue(result.contains("Abnormal"));
    }

    @Test
    void findTriggers_smokingVariants() {
        // "smok" should match smoking, smoke, smoker, etc.
        List<String> result = TriggerTerms.findTriggers("The patient started smoking.");
        assertTrue(result.contains("Smoking"));

        result = TriggerTerms.findTriggers("The patient used to smoke.");
        assertTrue(result.contains("Smoking"));
    }

    @Test
    void findTriggers_antibodyVariants() {
        // "antibod" should match antibody, antibodies
        List<String> result = TriggerTerms.findTriggers("Antibodies detected in blood work.");
        assertTrue(result.contains("Antibody"));

        result = TriggerTerms.findTriggers("Elevated antibody levels.");
        assertTrue(result.contains("Antibody"));
    }

    @Test
    void findTriggers_duplicateTermCountedOnce() {
        String text = "Abnormal readings. Still abnormal after treatment. Definitely abnormal.";
        List<String> result = TriggerTerms.findTriggers(text);
        assertEquals(1, result.stream().filter(t -> t.equals("Abnormal")).count());
    }

    @Test
    void findTriggers_allElevenTerms() {
        String text = "Hemoglobin A1C microalbumin height weight smoking abnormal " +
                "cholesterol dizziness relapse reaction antibody";
        List<String> result = TriggerTerms.findTriggers(text);
        assertEquals(11, result.size());
    }

    // ── Test with actual test case note data ────────────────────────────────

    @Test
    void findTriggers_testNone_patient1() {
        // PatId 1 — "feel very well" + "Weight is equal to or below recommended"
        String text = "The patient states that they \"feel very well.\" Weight is equal to or below what is recommended.";
        List<String> result = TriggerTerms.findTriggers(text);
        // Should find only "Weight" = 1 trigger → None
        assertEquals(1, result.size());
        assertTrue(result.contains("Weight"));
    }

    @Test
    void findTriggers_testBorderline_patient2() {
        // PatId 2 — two notes with "abnormal" and "reaction"
        String text = "The patient states that they feel a lot of stress at work. " +
                "They also complain that their hearing has been abnormal lately. " +
                "The patient states that they had a reaction to medication in the past three months. " +
                "They also note that their hearing continues to be abnormal.";
        List<String> result = TriggerTerms.findTriggers(text);
        assertTrue(result.contains("Abnormal"));
        assertTrue(result.contains("Reaction"));
        assertEquals(2, result.size());
    }
}
