package com.questack.automation.schedule;

import com.questack.automation.config.AutomationProperties;
import com.questack.automation.service.DailyAutomationService;
import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyAutomationScheduler {

    private final AutomationProperties automationProperties;
    private final DailyAutomationService dailyAutomationService;

    public DailyAutomationScheduler(
            AutomationProperties automationProperties,
            DailyAutomationService dailyAutomationService
    ) {
        this.automationProperties = automationProperties;
        this.dailyAutomationService = dailyAutomationService;
    }

    @Scheduled(cron = "${automation.daily.cron:-}")
    public void runDailyPipeline() {
        if (!automationProperties.daily().enabled()) {
            return;
        }
        dailyAutomationService.runDailyPipeline(LocalDate.now());
    }
}
