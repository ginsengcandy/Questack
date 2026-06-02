package com.questack.automation.service;

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
import org.springframework.stereotype.Service;

@Service
public class DailyAutomationService {

    private final AutomationProperties automationProperties;
    private final DailyAutomationBudgetGuard budgetGuard;
    private final GithubCollector githubCollector;
    private final RssCollector rssCollector;
    private final RankingService rankingService;
    private final DailyBriefingService dailyBriefingService;

    public DailyAutomationService(
            AutomationProperties automationProperties,
            DailyAutomationBudgetGuard budgetGuard,
            GithubCollector githubCollector,
            RssCollector rssCollector,
            RankingService rankingService,
            DailyBriefingService dailyBriefingService
    ) {
        this.automationProperties = automationProperties;
        this.budgetGuard = budgetGuard;
        this.githubCollector = githubCollector;
        this.rssCollector = rssCollector;
        this.rankingService = rankingService;
        this.dailyBriefingService = dailyBriefingService;
    }

    public DailyAutomationRunResult runDailyPipeline(LocalDate briefingDate) {
        budgetGuard.recordUsage(AutomationBudgetType.GITHUB_COLLECTION);
        GithubCollectionResult githubResult = githubCollector.collect(null, automationProperties.daily().githubPerPage());

        budgetGuard.recordUsage(AutomationBudgetType.RSS_COLLECTION);
        RssCollectionResult rssResult = rssCollector.collect();

        RankingRunResult rankingResult = rankingService.rankUnscoredItems();
        DailyBriefingResponse briefingResponse = dailyBriefingService.generate(briefingDate);

        return new DailyAutomationRunResult(
                briefingDate,
                githubResult.savedCount(),
                rssResult.savedCount(),
                rankingResult.scoredCount(),
                briefingResponse.itemCount()
        );
    }
}
