package com.questack.automation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "automation")
public record AutomationProperties(
        Daily daily,
        Cost cost
) {

    public record Daily(boolean enabled, String cron, int githubPerPage) {
    }

    public record Cost(
            int maxGithubCollectionRunsPerDay,
            int maxRssCollectionRunsPerDay,
            int maxLlmRequestsPerDay
    ) {
    }
}
