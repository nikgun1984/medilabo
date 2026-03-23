package com.medilabo.assessment.service;

import com.medilabo.assessment.model.RiskLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the risk-level determination logic in AssessmentService.
 * These tests call determineRiskLevel() directly (package-private).
 */
class RiskLevelDeterminationTest {

    private final AssessmentService service;

    RiskLevelDeterminationTest() {
        // Create service with dummy URLs since we only test the pure logic method
        this.service = new AssessmentService("http://dummy:8081", "http://dummy:8082");
    }

    // ── None ────────────────────────────────────────────────────────────────

    @Test
    void none_zeroTriggers() {
        assertEquals(RiskLevel.None, service.determineRiskLevel(50, "M", 0));
    }

    @Test
    void none_oneTrigger_over30() {
        assertEquals(RiskLevel.None, service.determineRiskLevel(45, "F", 1));
    }

    @Test
    void none_oneTrigger_under30_male() {
        assertEquals(RiskLevel.None, service.determineRiskLevel(25, "M", 1));
    }

    @Test
    void none_twoTriggers_under30_male() {
        assertEquals(RiskLevel.None, service.determineRiskLevel(25, "M", 2));
    }

    // ── Borderline (over 30, 2-5 triggers) ──────────────────────────────────

    @ParameterizedTest
    @CsvSource({"2", "3", "4", "5"})
    void borderline_over30(int triggers) {
        assertEquals(RiskLevel.Borderline, service.determineRiskLevel(35, "M", triggers));
        assertEquals(RiskLevel.Borderline, service.determineRiskLevel(40, "F", triggers));
    }

    @Test
    void borderline_notApplicable_under30() {
        // Under 30 with 2 triggers → None (for male) since Borderline requires over 30
        assertEquals(RiskLevel.None, service.determineRiskLevel(25, "M", 2));
    }

    // ── In Danger ───────────────────────────────────────────────────────────

    @Test
    void inDanger_maleUnder30_3triggers() {
        assertEquals(RiskLevel.InDanger, service.determineRiskLevel(25, "M", 3));
    }

    @Test
    void inDanger_maleUnder30_4triggers() {
        assertEquals(RiskLevel.InDanger, service.determineRiskLevel(25, "M", 4));
    }

    @Test
    void inDanger_femaleUnder30_4triggers() {
        assertEquals(RiskLevel.InDanger, service.determineRiskLevel(25, "F", 4));
    }

    @Test
    void inDanger_femaleUnder30_5triggers() {
        assertEquals(RiskLevel.InDanger, service.determineRiskLevel(25, "F", 5));
    }

    @Test
    void inDanger_over30_6triggers() {
        assertEquals(RiskLevel.InDanger, service.determineRiskLevel(45, "M", 6));
    }

    @Test
    void inDanger_over30_7triggers() {
        assertEquals(RiskLevel.InDanger, service.determineRiskLevel(45, "F", 7));
    }

    // ── Early Onset ─────────────────────────────────────────────────────────

    @Test
    void earlyOnset_maleUnder30_5triggers() {
        assertEquals(RiskLevel.EarlyOnset, service.determineRiskLevel(25, "M", 5));
    }

    @Test
    void earlyOnset_maleUnder30_8triggers() {
        assertEquals(RiskLevel.EarlyOnset, service.determineRiskLevel(25, "M", 8));
    }

    @Test
    void earlyOnset_femaleUnder30_6triggers() {
        assertEquals(RiskLevel.EarlyOnset, service.determineRiskLevel(25, "F", 6));
    }

    @Test
    void earlyOnset_femaleUnder30_10triggers() {
        assertEquals(RiskLevel.EarlyOnset, service.determineRiskLevel(25, "F", 10));
    }

    @Test
    void earlyOnset_over30_8triggers() {
        assertEquals(RiskLevel.EarlyOnset, service.determineRiskLevel(50, "M", 8));
    }

    @Test
    void earlyOnset_over30_11triggers() {
        assertEquals(RiskLevel.EarlyOnset, service.determineRiskLevel(50, "F", 11));
    }

    // ── Boundary conditions ─────────────────────────────────────────────────

    @Test
    void age30_isTreatedAsOver30() {
        // Age 30 is NOT "under 30" → the over-30 rules apply
        assertEquals(RiskLevel.Borderline, service.determineRiskLevel(30, "M", 2));
    }

    @Test
    void age29_isTreatedAsUnder30() {
        assertEquals(RiskLevel.None, service.determineRiskLevel(29, "M", 2));
    }
}
