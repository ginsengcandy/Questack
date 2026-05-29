package com.questack.collection.rss.service;

import com.questack.collection.CollectedItem;
import com.questack.collection.CollectedItemRepository;
import com.questack.collection.CollectedItemType;
import com.questack.collection.rss.api.dto.RssCollectionResult;
import com.questack.collection.rss.config.RssFeedProperties;
import com.questack.collection.rss.config.RssProperties;
import com.questack.source.Source;
import com.questack.source.SourceRepository;
import com.questack.source.SourceType;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
public class RssCollector {

    private final RssProperties rssProperties;
    private final RssFeedParser rssFeedParser;
    private final SourceRepository sourceRepository;
    private final CollectedItemRepository collectedItemRepository;
    private final RestClient restClient;

    public RssCollector(
            RssProperties rssProperties,
            RssFeedParser rssFeedParser,
            SourceRepository sourceRepository,
            CollectedItemRepository collectedItemRepository,
            RestClient.Builder restClientBuilder
    ) {
        this.rssProperties = rssProperties;
        this.rssFeedParser = rssFeedParser;
        this.sourceRepository = sourceRepository;
        this.collectedItemRepository = collectedItemRepository;
        this.restClient = restClientBuilder
                .defaultHeader(HttpHeaders.USER_AGENT, "Questack")
                .build();
    }

    @Transactional
    public RssCollectionResult collect() {
        List<RssFeedProperties> feeds = rssProperties.feeds() == null ? List.of() : rssProperties.feeds();
        int fetchedCount = 0;
        int savedCount = 0;
        int skippedDuplicateCount = 0;

        for (RssFeedProperties feed : feeds) {
            Source source = sourceRepository.findByName(feed.name())
                    .orElseGet(() -> sourceRepository.save(new Source(feed.name(), SourceType.RSS, feed.url(), feed.priority())));
            List<RssFeedItem> items = fetchItems(feed.url());
            fetchedCount += items.size();

            for (RssFeedItem item : items) {
                if (item.link().isBlank() || collectedItemRepository.existsByCanonicalUrl(item.link())) {
                    skippedDuplicateCount++;
                    continue;
                }

                collectedItemRepository.save(new CollectedItem(
                        source,
                        CollectedItemType.BLOG_POST,
                        RssTextNormalizer.title(item.title()),
                        RssTextNormalizer.summary(item.description()),
                        item.link(),
                        RssTextNormalizer.externalId(item.link()),
                        RssTextNormalizer.author(item.author()),
                        item.publishedAt()
                ));
                savedCount++;
            }
        }

        return new RssCollectionResult(feeds.size(), fetchedCount, savedCount, skippedDuplicateCount);
    }

    private List<RssFeedItem> fetchItems(String feedUrl) {
        String xml = restClient.get()
                .uri(feedUrl)
                .retrieve()
                .body(String.class);
        if (xml == null || xml.isBlank()) {
            return List.of();
        }
        return rssFeedParser.parse(xml);
    }
}
