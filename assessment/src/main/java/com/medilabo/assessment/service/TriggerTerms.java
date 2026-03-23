package com.medilabo.assessment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class that scans physician notes for medical trigger terms
 * related to diabetes risk assessment.
 */
public final class TriggerTerms {

    private TriggerTerms() {}

    /**
     * The 11 trigger terms to look for in physician notes.
     * Matching is case-insensitive and looks for whole words where relevant.
     */
    private static final List<TriggerEntry> TRIGGERS = List.of(
            trigger("Hemoglobin A1C", "hemoglobin a1c"),
            trigger("Microalbumin",   "microalbumin"),
            trigger("Height",         "height"),
            trigger("Weight",         "weight"),
            trigger("Smoking",        "smok"),         // matches "smoking", "smoke", "smoker"
            trigger("Abnormal",       "abnormal"),
            trigger("Cholesterol",    "cholesterol"),
            trigger("Dizziness",      "dizziness"),
            trigger("Relapse",        "relapse"),
            trigger("Reaction",       "reaction"),
            trigger("Antibody",       "antibod")       // matches "antibody", "antibodies"
    );

    /**
     * Count the number of distinct trigger terms found in the combined notes text.
     *
     * @param notesText all physician notes concatenated into a single string
     * @return list of trigger term labels that were found
     */
    public static List<String> findTriggers(String notesText) {
        if (notesText == null || notesText.isBlank()) {
            return List.of();
        }

        String lower = notesText.toLowerCase();
        List<String> found = new ArrayList<>();

        for (TriggerEntry entry : TRIGGERS) {
            if (lower.contains(entry.searchToken())) {
                found.add(entry.label());
            }
        }

        return found;
    }

    private static TriggerEntry trigger(String label, String searchToken) {
        return new TriggerEntry(label, searchToken);
    }

    private record TriggerEntry(String label, String searchToken) {}
}
