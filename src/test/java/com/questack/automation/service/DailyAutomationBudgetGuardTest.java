package com.questack.automation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.questack.automation.config.AutomationProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class DailyAutomationBudgetGuardTest {

    @Test
    void allowsUsageUntilDailyBudgetIsReached() {
        DailyAutomationBudgetGuard guard = new DailyAutomationBudgetGuard(properties(), fixedClock("2026-06-02T00:00:00Z"));

        guard.recordUsage(AutomationBudgetType.GITHUB_COLLECTION);

        assertThat(guard.usageCount(AutomationBudgetType.GITHUB_COLLECTION)).isEqualTo(1);
        assertThatThrownBy(() -> guard.recordUsage(AutomationBudgetType.GITHUB_COLLECTION))
                .isInstanceOf(AutomationBudgetExceededException.class)
                .hasMessageContaining("GITHUB_COLLECTION daily budget exceeded");
    }

    @Test
    void blocksLlmRequestsWhenDailyBudgetIsZero() {
        DailyAutomationBudgetGuard guard = new DailyAutomationBudgetGuard(properties(), fixedClock("2026-06-02T00:00:00Z"));

        assertThatThrownBy(() -> guard.recordUsage(AutomationBudgetType.LLM_REQUEST))
                .isInstanceOf(AutomationBudgetExceededException.class)
                .hasMessageContaining("LLM_REQUEST daily budget exceeded");
    }

    private AutomationProperties properties() {
        return new AutomationProperties(
                new AutomationProperties.Daily(false, "-", 10),
                new AutomationProperties.Cost(1, 1, 0)
        );
    }

    private Clock fixedClock(String instant) {
        return Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
    }
}
