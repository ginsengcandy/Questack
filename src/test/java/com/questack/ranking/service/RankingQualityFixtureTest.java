package com.questack.ranking.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.questack.collection.CollectedItem;
import com.questack.collection.CollectedItemRepository;
import com.questack.collection.CollectedItemType;
import com.questack.ranking.RankingScoreRepository;
import com.questack.ranking.api.dto.RankingRunResult;
import com.questack.ranking.api.dto.RankingScoreResponse;
import com.questack.source.Source;
import com.questack.source.SourceRepository;
import com.questack.source.SourceType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

@SpringBootTest
class RankingQualityFixtureTest {

    private static final String FIXTURE_PATH = "fixtures/ranking/labeled-collected-items.json";

    @Autowired
    private RankingService rankingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private CollectedItemRepository collectedItemRepository;

    @Autowired
    private RankingScoreRepository rankingScoreRepository;

    @BeforeEach
    void setUp() {
        rankingScoreRepository.deleteAll();
        collectedItemRepository.deleteAll();
        sourceRepository.deleteAll();
    }

    @Test
    void keepsBackendCareerRelevantItemsInTopThree() throws Exception {
        LabeledRankingFixture fixture = loadFixture();
        saveFixtureItems(fixture.items());

        RankingRunResult result = rankingService.rankUnscoredItems();
        List<RankingScoreResponse> topRankings = rankingService.findTopRankings(3);
        List<String> topTitles = topRankings.stream()
                .map(RankingScoreResponse::title)
                .toList();
        List<String> usefulTitles = fixture.items().stream()
                .filter(LabeledCollectedItemFixture::useful)
                .map(LabeledCollectedItemFixture::title)
                .toList();
        List<String> notUsefulTitles = fixture.items().stream()
                .filter(item -> !item.useful())
                .map(LabeledCollectedItemFixture::title)
                .toList();

        assertThat(result.candidateCount()).isEqualTo(fixture.items().size());
        assertThat(result.scoredCount()).isEqualTo(fixture.items().size());
        assertThat(result.skippedAlreadyScoredCount()).isZero();
        assertThat(topRankings).hasSize(3);
        assertThat(topTitles).containsExactlyInAnyOrderElementsOf(usefulTitles);
        assertThat(topTitles).doesNotContainAnyElementsOf(notUsefulTitles);
        assertThat(topRankings)
                .allSatisfy(ranking -> {
                    assertThat(ranking.totalScore()).isPositive();
                    assertThat(ranking.reasons()).doesNotContain("No matching backend relevance rules");
                });
        assertThat(topRankings.getFirst().reasons())
                .contains("Spring backend relevance")
                .contains("Spring AI backend application");
    }

    @Test
    void keepsFrontendHardwareAndBusinessOnlyItemsBelowTopThree() throws Exception {
        LabeledRankingFixture fixture = loadFixture();
        saveFixtureItems(fixture.items());

        rankingService.rankUnscoredItems();
        List<RankingScoreResponse> rankings = rankingService.findTopRankings(10);

        Map<String, Integer> scoreByTitle = rankings.stream()
                .collect(Collectors.toMap(RankingScoreResponse::title, RankingScoreResponse::totalScore));

        int lowestUsefulScore = fixture.items().stream()
                .filter(LabeledCollectedItemFixture::useful)
                .mapToInt(item -> scoreByTitle.get(item.title()))
                .min()
                .orElseThrow();

        fixture.items().stream()
                .filter(item -> !item.useful())
                .forEach(item -> assertThat(scoreByTitle.get(item.title()))
                        .as(item.title())
                        .isLessThan(lowestUsefulScore));
    }

    private void saveFixtureItems(List<LabeledCollectedItemFixture> items) {
        Map<String, Source> sources = items.stream()
                .map(item -> Map.entry(item.sourceName(), item.sourceType()))
                .distinct()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> sourceRepository.save(new Source(
                                entry.getKey(),
                                entry.getValue(),
                                "https://example.com/" + entry.getKey().toLowerCase().replace(" ", "-"),
                                1
                        ))
                ));

        for (int index = 0; index < items.size(); index++) {
            LabeledCollectedItemFixture item = items.get(index);
            collectedItemRepository.save(new CollectedItem(
                    sources.get(item.sourceName()),
                    item.itemType(),
                    item.title(),
                    item.summary(),
                    item.canonicalUrl(),
                    String.valueOf(index + 1),
                    "fixture",
                    Instant.parse("2026-06-01T00:00:00Z").plusSeconds(index)
            ));
        }
    }

    private LabeledRankingFixture loadFixture() throws Exception {
        return objectMapper.readValue(
                new ClassPathResource(FIXTURE_PATH).getInputStream(),
                LabeledRankingFixture.class
        );
    }

    private record LabeledRankingFixture(
            List<LabeledCollectedItemFixture> items
    ) {
    }

    private record LabeledCollectedItemFixture(
            String sourceName,
            SourceType sourceType,
            CollectedItemType itemType,
            String title,
            String summary,
            String canonicalUrl,
            String label
    ) {

        boolean useful() {
            return "useful".equals(label);
        }
    }
}
