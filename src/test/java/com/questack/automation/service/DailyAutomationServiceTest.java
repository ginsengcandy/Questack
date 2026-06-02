package com.questack.automation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import com.questack.automation.config.AutomationProperties;
import com.questack.briefing.api.dto.DailyBriefingResponse;
import com.questack.briefing.service.DailyBriefingService;
import com.questack.collection.github.api.dto.GithubCollectionResult;
import com.questack.collection.github.service.GithubCollector;
import com.questack.collection.rss.api.dto.RssCollectionResult;
import com.questack.collection.rss.service.RssCollector;
import com.questack.ranking.api.dto.RankingRunResult;
import com.questack.ranking.service.RankingService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

class DailyAutomationServiceTest {

    private final AutomationProperties properties = new AutomationProperties(
            new AutomationProperties.Daily(false, "-", 5),
            new AutomationProperties.Cost(1, 1, 0)
    );
    private final DailyAutomationBudgetGuard budgetGuard = new DailyAutomationBudgetGuard(properties);
    private final GithubCollector githubCollector = Mockito.mock(GithubCollector.class);
    private final RssCollector rssCollector = Mockito.mock(RssCollector.class);
    private final RankingService rankingService = Mockito.mock(RankingService.class);
    private final DailyBriefingService dailyBriefingService = Mockito.mock(DailyBriefingService.class);
    private final DailyAutomationService service = new DailyAutomationService(
            properties,
            budgetGuard,
            githubCollector,
            rssCollector,
            rankingService,
            dailyBriefingService
    );

    @Test
    void runsManualPipelineInCollectionRankingBriefingOrderWithBudgets() {
        LocalDate briefingDate = LocalDate.of(2026, 6, 2);
        when(githubCollector.collect(null, 5)).thenReturn(new GithubCollectionResult(5, 4, 1));
        when(rssCollector.collect()).thenReturn(new RssCollectionResult(5, 12, 10, 2, 0, List.of()));
        when(rankingService.rankUnscoredItems()).thenReturn(new RankingRunResult(14, 7, 7));
        when(dailyBriefingService.generate(briefingDate))
                .thenReturn(new DailyBriefingResponse(briefingDate, "docs/daily-briefings/2026-06-02.md", 3, "# briefing"));

        DailyAutomationRunResult result = service.runDailyPipeline(briefingDate);

        assertThat(result.githubSavedCount()).isEqualTo(4);
        assertThat(result.rssSavedCount()).isEqualTo(10);
        assertThat(result.rankingScoredCount()).isEqualTo(7);
        assertThat(result.briefingItemCount()).isEqualTo(3);
        assertThat(budgetGuard.usageCount(AutomationBudgetType.GITHUB_COLLECTION)).isEqualTo(1);
        assertThat(budgetGuard.usageCount(AutomationBudgetType.RSS_COLLECTION)).isEqualTo(1);

        InOrder inOrder = inOrder(githubCollector, rssCollector, rankingService, dailyBriefingService);
        inOrder.verify(githubCollector).collect(null, 5);
        inOrder.verify(rssCollector).collect();
        inOrder.verify(rankingService).rankUnscoredItems();
        inOrder.verify(dailyBriefingService).generate(briefingDate);
    }
}
