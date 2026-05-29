package com.questack.collection.rss.config;

public record RssFeedProperties(
        String name,
        String url,
        int priority
) {
}
