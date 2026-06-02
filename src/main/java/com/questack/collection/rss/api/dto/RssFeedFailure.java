package com.questack.collection.rss.api.dto;

public record RssFeedFailure(
        String feedName,
        String feedUrl,
        String reason
) {
}
