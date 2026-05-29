package com.questack.collection.rss.api.dto;

public record RssCollectionResult(
        int feedCount,
        int fetchedCount,
        int savedCount,
        int skippedDuplicateCount
) {
}
