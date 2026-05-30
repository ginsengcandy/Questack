package com.questack.collection.github.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.questack.collection.CollectedItem;
import com.questack.collection.CollectedItemRepository;
import com.questack.collection.CollectedItemType;
import com.questack.collection.github.api.dto.GithubCollectionResult;
import com.questack.collection.github.client.GithubSearchClient;
import com.questack.collection.github.client.dto.GithubRepositoryItem;
import com.questack.collection.github.client.dto.GithubRepositorySearchResponse;
import com.questack.ranking.RankingScoreRepository;
import com.questack.source.Source;
import com.questack.source.SourceRepository;
import com.questack.source.SourceType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class GithubCollectorReplayTest {

    private static final String FIXTURE_PATH = "fixtures/github/search-repositories.json";

    @Autowired
    private GithubCollector githubCollector;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CollectedItemRepository collectedItemRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private RankingScoreRepository rankingScoreRepository;

    @MockitoBean
    private GithubSearchClient githubSearchClient;

    @BeforeEach
    void setUp() {
        rankingScoreRepository.deleteAll();
        collectedItemRepository.deleteAll();
        sourceRepository.deleteAll();
    }

    @Test
    void replaysGithubSearchFixtureIntoCollectedItems() throws Exception {
        when(githubSearchClient.searchRepositories(anyString(), anyInt()))
                .thenReturn(fixtureItems());

        GithubCollectionResult result = githubCollector.collect("language:Java topic:spring-boot", 2);

        assertThat(result.fetchedCount()).isEqualTo(2);
        assertThat(result.savedCount()).isEqualTo(2);
        assertThat(result.skippedDuplicateCount()).isZero();

        Source source = sourceRepository.findByName("GitHub Search").orElseThrow();
        assertThat(source.getType()).isEqualTo(SourceType.GITHUB);
        assertThat(source.getUrl()).isEqualTo("https://github.com/search");

        CollectedItem springAi = collectedItemRepository
                .findByCanonicalUrl("https://github.com/example/spring-ai-rag-service")
                .orElseThrow();
        assertThat(springAi.getSource().getId()).isEqualTo(source.getId());
        assertThat(springAi.getType()).isEqualTo(CollectedItemType.GITHUB_REPOSITORY);
        assertThat(springAi.getTitle()).isEqualTo("example/spring-ai-rag-service");
        assertThat(springAi.getSummary())
                .isEqualTo("language=Java, stars=421, description=Java Spring Boot RAG service with Redis caching and JWT authentication");
        assertThat(springAi.getExternalId()).isEqualTo("1001");
        assertThat(springAi.getAuthor()).isEqualTo("example");
        assertThat(springAi.getPublishedAt()).isEqualTo(Instant.parse("2026-05-28T10:15:30Z"));

        CollectedItem kafka = collectedItemRepository
                .findByCanonicalUrl("https://github.com/example/kafka-order-retry-lab")
                .orElseThrow();
        assertThat(kafka.getTitle()).isEqualTo("example/kafka-order-retry-lab");
        assertThat(kafka.getSummary()).contains("dead-letter queue");
    }

    @Test
    void skipsDuplicateCanonicalUrlsWhenFixtureIsReplayedAgain() throws Exception {
        List<GithubRepositoryItem> fixtureItems = fixtureItems();
        when(githubSearchClient.searchRepositories(anyString(), anyInt()))
                .thenReturn(fixtureItems)
                .thenReturn(fixtureItems);

        GithubCollectionResult firstRun = githubCollector.collect("language:Java topic:spring-boot", 2);
        GithubCollectionResult secondRun = githubCollector.collect("language:Java topic:spring-boot", 2);

        assertThat(firstRun.savedCount()).isEqualTo(2);
        assertThat(firstRun.skippedDuplicateCount()).isZero();
        assertThat(secondRun.fetchedCount()).isEqualTo(2);
        assertThat(secondRun.savedCount()).isZero();
        assertThat(secondRun.skippedDuplicateCount()).isEqualTo(2);
        assertThat(collectedItemRepository.count()).isEqualTo(2);
        assertThat(sourceRepository.count()).isEqualTo(1);
    }

    private List<GithubRepositoryItem> fixtureItems() throws Exception {
        GithubRepositorySearchResponse response = objectMapper.readValue(
                new ClassPathResource(FIXTURE_PATH).getInputStream(),
                GithubRepositorySearchResponse.class
        );
        return response.items();
    }
}
