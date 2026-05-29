package com.questack.collection.rss.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rss")
public record RssProperties(
        List<RssFeedProperties> feeds
) {
}
