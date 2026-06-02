package com.questack.automation.service;

import com.questack.automation.config.AutomationProperties;
import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DailyAutomationBudgetGuard {

    private final AutomationProperties automationProperties;
    private final Clock clock;
    private LocalDate currentDate;
    private final Map<AutomationBudgetType, Integer> usageCounts = new EnumMap<>(AutomationBudgetType.class);

    @Autowired
    public DailyAutomationBudgetGuard(AutomationProperties automationProperties) {
        this(automationProperties, Clock.systemDefaultZone());
    }

    DailyAutomationBudgetGuard(AutomationProperties automationProperties, Clock clock) {
        this.automationProperties = automationProperties;
        this.clock = clock;
        this.currentDate = LocalDate.now(clock);
    }

    public synchronized void recordUsage(AutomationBudgetType type) {
        resetIfNewDay();
        int maxDailyUsage = maxDailyUsage(type);
        int currentUsage = usageCounts.getOrDefault(type, 0);
        if (currentUsage >= maxDailyUsage) {
            throw new AutomationBudgetExceededException(
                    "%s daily budget exceeded: %d/%d".formatted(type, currentUsage, maxDailyUsage)
            );
        }
        usageCounts.put(type, currentUsage + 1);
    }

    public synchronized int usageCount(AutomationBudgetType type) {
        resetIfNewDay();
        return usageCounts.getOrDefault(type, 0);
    }

    private void resetIfNewDay() {
        LocalDate today = LocalDate.now(clock);
        if (!today.equals(currentDate)) {
            usageCounts.clear();
            currentDate = today;
        }
    }

    private int maxDailyUsage(AutomationBudgetType type) {
        AutomationProperties.Cost cost = automationProperties.cost();
        return switch (type) {
            case GITHUB_COLLECTION -> cost.maxGithubCollectionRunsPerDay();
            case RSS_COLLECTION -> cost.maxRssCollectionRunsPerDay();
            case LLM_REQUEST -> cost.maxLlmRequestsPerDay();
        };
    }
}
