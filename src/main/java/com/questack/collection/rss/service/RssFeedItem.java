package com.questack.collection.rss.service;

import java.time.Instant;

record RssFeedItem(
        String title,
        String description,
        String link,
        String author,
        Instant publishedAt
) {
}
