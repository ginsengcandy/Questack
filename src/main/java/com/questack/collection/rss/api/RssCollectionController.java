package com.questack.collection.rss.api;

import com.questack.collection.rss.api.dto.RssCollectionResult;
import com.questack.collection.rss.service.RssCollector;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RssCollectionController {

    private final RssCollector rssCollector;

    public RssCollectionController(RssCollector rssCollector) {
        this.rssCollector = rssCollector;
    }

    @PostMapping("/collections/rss")
    public RssCollectionResult collectRssFeeds() {
        return rssCollector.collect();
    }
}
