package com.questack.ranking.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.questack.collection.CollectedItem;
import com.questack.collection.CollectedItemRepository;
import com.questack.collection.CollectedItemType;
import com.questack.ranking.api.dto.RankingRunResult;
import com.questack.ranking.api.dto.RankingScoreResponse;
import com.questack.source.Source;
import com.questack.source.SourceRepository;
import com.questack.source.SourceType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RankingServiceTest {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private CollectedItemRepository collectedItemRepository;

    @Test
    void ranksBackendRelevantItemsAboveFrontendHeavyItems() {
        Source source = sourceRepository.save(new Source("Test GitHub", SourceType.GITHUB, "https://github.com", 1));
        collectedItemRepository.save(new CollectedItem(
                source,
                CollectedItemType.GITHUB_REPOSITORY,
                "spring-ai-rag-service",
                "Java Spring Boot RAG service with Redis and JWT",
                "https://github.com/example/spring-ai-rag-service",
                "1",
                "example",
                Instant.now()
        ));
        collectedItemRepository.save(new CollectedItem(
                source,
                CollectedItemType.GITHUB_REPOSITORY,
                "react-css-dashboard",
                "Frontend React CSS dashboard",
                "https://github.com/example/react-css-dashboard",
                "2",
                "example",
                Instant.now()
        ));

        RankingRunResult result = rankingService.rankUnscoredItems();
        List<RankingScoreResponse> topRankings = rankingService.findTopRankings(2);

        assertThat(result.candidateCount()).isEqualTo(2);
        assertThat(result.scoredCount()).isEqualTo(2);
        assertThat(result.skippedAlreadyScoredCount()).isZero();
        assertThat(topRankings).hasSize(2);
        assertThat(topRankings.getFirst().title()).isEqualTo("spring-ai-rag-service");
        assertThat(topRankings.getFirst().summary()).isEqualTo("Java Spring Boot RAG service with Redis and JWT");
        assertThat(topRankings.getFirst().totalScore()).isGreaterThan(topRankings.getLast().totalScore());
    }
}
