package com.questack.automation.service;

import java.time.LocalDate;

public record DailyAutomationRunResult(
        LocalDate briefingDate,
        int githubSavedCount,
        int rssSavedCount,
        int rankingScoredCount,
        int briefingItemCount
) {
}
