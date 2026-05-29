package com.questack.collection.rss.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RssTextNormalizerTest {

    @Test
    void stripsHtmlAndTruncatesSummaryToCollectedItemLimit() {
        String longHtml = "<p><strong>Spring</strong> Boot</p>" + "a".repeat(2100);

        String summary = RssTextNormalizer.summary(longHtml);

        assertThat(summary).doesNotContain("<p>", "<strong>");
        assertThat(summary).startsWith("Spring Boot");
        assertThat(summary).hasSize(2000);
    }

    @Test
    void truncatesExternalIdToCollectedItemLimit() {
        String externalId = RssTextNormalizer.externalId("https://example.com/" + "a".repeat(200));

        assertThat(externalId).hasSize(100);
    }
}
