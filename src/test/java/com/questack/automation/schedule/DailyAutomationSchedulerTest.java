package com.questack.automation.schedule;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.questack.automation.config.AutomationProperties;
import com.questack.automation.service.DailyAutomationService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DailyAutomationSchedulerTest {

    @Test
    void doesNotRunPipelineWhenAutomationIsDisabled() {
        DailyAutomationService service = Mockito.mock(DailyAutomationService.class);
        DailyAutomationScheduler scheduler = new DailyAutomationScheduler(
                new AutomationProperties(
                        new AutomationProperties.Daily(false, "-", 10),
                        new AutomationProperties.Cost(1, 1, 0)
                ),
                service
        );

        scheduler.runDailyPipeline();

        verify(service, never()).runDailyPipeline(Mockito.any(LocalDate.class));
    }
}
