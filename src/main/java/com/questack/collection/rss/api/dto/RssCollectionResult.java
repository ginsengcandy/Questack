package com.questack.collection.rss.api.dto;

import java.util.List;

public record RssCollectionResult(
        int feedCount,
        int fetchedCount,
        int savedCount,
        int skippedDuplicateCount,
        int failedFeedCount,
        List<RssFeedFailure> failedFeeds
) {

    public RssCollectionResult {
        failedFeeds = List.copyOf(failedFeeds);
    }
}
