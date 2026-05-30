package com.questack.collection.rss.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.questack.collection.CollectedItem;
import com.questack.collection.CollectedItemRepository;
import com.questack.collection.CollectedItemType;
import com.questack.collection.rss.api.dto.RssCollectionResult;
import com.questack.collection.rss.config.RssFeedProperties;
import com.questack.collection.rss.config.RssProperties;
import com.questack.ranking.RankingScoreRepository;
import com.questack.source.Source;
import com.questack.source.SourceRepository;
import com.questack.source.SourceType;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@DataJpaTest
class RssCollectorReplayTest {

    private static final String RSS_FEED_URL = "https://feeds.example.com/backend.xml";
    private static final String ATOM_FEED_URL = "https://feeds.example.com/ai.atom";

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private CollectedItemRepository collectedItemRepository;

    @Autowired
    private RankingScoreRepository rankingScoreRepository;

    private MockRestServiceServer server;
    private RssCollector rssCollector;

    @BeforeEach
    void setUp() {
        rankingScoreRepository.deleteAll();
        collectedItemRepository.deleteAll();
        sourceRepository.deleteAll();

        RestClient.Builder restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        rssCollector = new RssCollector(
                new RssProperties(List.of(
                        new RssFeedProperties("Backend Blog", RSS_FEED_URL, 1),
                        new RssFeedProperties("AI Backend Blog", ATOM_FEED_URL, 2)
                )),
                new RssFeedParser(),
                sourceRepository,
                collectedItemRepository,
                restClientBuilder
        );
    }

    @Test
    void replaysRssAndAtomFixturesIntoCollectedItems() throws Exception {
        expectFeed(RSS_FEED_URL, "fixtures/rss/backend-blog-rss.xml");
        expectFeed(ATOM_FEED_URL, "fixtures/rss/backend-blog-atom.xml");

        RssCollectionResult result = rssCollector.collect();

        assertThat(result.feedCount()).isEqualTo(2);
        assertThat(result.fetchedCount()).isEqualTo(3);
        assertThat(result.savedCount()).isEqualTo(2);
        assertThat(result.skippedDuplicateCount()).isEqualTo(1);
        server.verify();

        Source rssSource = sourceRepository.findByName("Backend Blog").orElseThrow();
        assertThat(rssSource.getType()).isEqualTo(SourceType.RSS);
        assertThat(rssSource.getUrl()).isEqualTo(RSS_FEED_URL);
        assertThat(rssSource.getPriority()).isEqualTo(1);

        CollectedItem cacheItem = collectedItemRepository
                .findByCanonicalUrl("https://example.com/cache")
                .orElseThrow();
        assertThat(cacheItem.getSource().getId()).isEqualTo(rssSource.getId());
        assertThat(cacheItem.getType()).isEqualTo(CollectedItemType.BLOG_POST);
        assertThat(cacheItem.getTitle()).isEqualTo("Spring Boot Cache Strategy");
        assertThat(cacheItem.getSummary()).isEqualTo("Redis cache invalidation in Spring Boot");
        assertThat(cacheItem.getExternalId()).isEqualTo("https://example.com/cache");
        assertThat(cacheItem.getAuthor()).isEqualTo("backend-team");
        assertThat(cacheItem.getPublishedAt()).isEqualTo(Instant.parse("2026-05-29T10:00:00Z"));

        Source atomSource = sourceRepository.findByName("AI Backend Blog").orElseThrow();
        assertThat(atomSource.getPriority()).isEqualTo(2);

        CollectedItem springAiItem = collectedItemRepository
                .findByCanonicalUrl("https://example.com/spring-ai-rag")
                .orElseThrow();
        assertThat(springAiItem.getSource().getId()).isEqualTo(atomSource.getId());
        assertThat(springAiItem.getTitle()).isEqualTo("Spring AI RAG Release Notes");
        assertThat(springAiItem.getSummary()).contains("retrieval, generation, and fallback");
        assertThat(springAiItem.getAuthor()).isEqualTo("ai-team");
        assertThat(springAiItem.getPublishedAt()).isEqualTo(Instant.parse("2026-05-29T12:00:00Z"));
    }

    @Test
    void skipsDuplicateCanonicalUrlsWhenFixturesAreReplayedAgain() throws Exception {
        expectFeed(RSS_FEED_URL, "fixtures/rss/backend-blog-rss.xml");
        expectFeed(ATOM_FEED_URL, "fixtures/rss/backend-blog-atom.xml");
        RssCollectionResult firstRun = rssCollector.collect();
        server.verify();
        server.reset();

        expectFeed(RSS_FEED_URL, "fixtures/rss/backend-blog-rss.xml");
        expectFeed(ATOM_FEED_URL, "fixtures/rss/backend-blog-atom.xml");
        RssCollectionResult secondRun = rssCollector.collect();

        assertThat(firstRun.savedCount()).isEqualTo(2);
        assertThat(firstRun.skippedDuplicateCount()).isEqualTo(1);
        assertThat(secondRun.fetchedCount()).isEqualTo(3);
        assertThat(secondRun.savedCount()).isZero();
        assertThat(secondRun.skippedDuplicateCount()).isEqualTo(3);
        assertThat(collectedItemRepository.count()).isEqualTo(2);
        assertThat(sourceRepository.count()).isEqualTo(2);
        server.verify();
    }

    private void expectFeed(String feedUrl, String fixturePath) throws Exception {
        server.expect(requestTo(feedUrl))
                .andRespond(withSuccess(fixture(fixturePath), MediaType.APPLICATION_XML));
    }

    private String fixture(String fixturePath) throws Exception {
        return new ClassPathResource(fixturePath).getContentAsString(StandardCharsets.UTF_8);
    }
}
