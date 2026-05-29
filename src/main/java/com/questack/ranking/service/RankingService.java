package com.questack.ranking.service;

import com.questack.collection.CollectedItem;
import com.questack.collection.CollectedItemRepository;
import com.questack.ranking.RankingScore;
import com.questack.ranking.RankingScoreRepository;
import com.questack.ranking.api.dto.RankingRunResult;
import com.questack.ranking.api.dto.RankingScoreResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RankingService {

    private static final List<KeywordScoreRule> POSITIVE_RULES = List.of(
            new KeywordScoreRule("java", 3, 2, 2, "Java/JVM relevance"),
            new KeywordScoreRule("spring", 4, 3, 3, "Spring backend relevance"),
            new KeywordScoreRule("spring boot", 5, 3, 3, "Spring Boot implementation value"),
            new KeywordScoreRule("spring ai", 5, 4, 4, "Spring AI backend application"),
            new KeywordScoreRule("jpa", 3, 2, 2, "JPA persistence topic"),
            new KeywordScoreRule("hibernate", 3, 2, 2, "Hibernate persistence topic"),
            new KeywordScoreRule("postgres", 3, 2, 2, "Database topic"),
            new KeywordScoreRule("mysql", 3, 2, 2, "Database topic"),
            new KeywordScoreRule("redis", 3, 2, 3, "Cache implementation topic"),
            new KeywordScoreRule("kafka", 4, 3, 4, "Messaging and event-driven topic"),
            new KeywordScoreRule("rabbitmq", 3, 2, 3, "Messaging topic"),
            new KeywordScoreRule("oauth", 3, 2, 3, "Authentication and authorization topic"),
            new KeywordScoreRule("jwt", 3, 2, 3, "Token authentication topic"),
            new KeywordScoreRule("docker", 2, 2, 2, "Deployment topic"),
            new KeywordScoreRule("kubernetes", 3, 3, 2, "Infrastructure topic"),
            new KeywordScoreRule("observability", 3, 3, 2, "Operational backend topic"),
            new KeywordScoreRule("rag", 4, 4, 4, "AI backend retrieval topic"),
            new KeywordScoreRule("llm", 3, 4, 3, "AI backend application topic"),
            new KeywordScoreRule("agent", 3, 3, 3, "AI agent backend topic"),
            new KeywordScoreRule("mcp", 3, 3, 3, "AI tool integration topic")
    );

    private static final List<KeywordScoreRule> NEGATIVE_RULES = List.of(
            new KeywordScoreRule("react", -3, -1, -1, "Frontend-heavy topic"),
            new KeywordScoreRule("vue", -3, -1, -1, "Frontend-heavy topic"),
            new KeywordScoreRule("css", -3, -1, -1, "UI styling topic"),
            new KeywordScoreRule("frontend", -3, -1, -1, "Frontend-heavy topic"),
            new KeywordScoreRule("robot", -4, -2, -2, "Robotics topic"),
            new KeywordScoreRule("battery", -4, -2, -2, "Battery topic"),
            new KeywordScoreRule("semiconductor", -4, -2, -2, "Semiconductor topic"),
            new KeywordScoreRule("chip", -3, -1, -1, "Hardware topic")
    );

    private final CollectedItemRepository collectedItemRepository;
    private final RankingScoreRepository rankingScoreRepository;

    public RankingService(
            CollectedItemRepository collectedItemRepository,
            RankingScoreRepository rankingScoreRepository
    ) {
        this.collectedItemRepository = collectedItemRepository;
        this.rankingScoreRepository = rankingScoreRepository;
    }

    @Transactional
    public RankingRunResult rankUnscoredItems() {
        List<CollectedItem> candidates = collectedItemRepository.findAll();
        int scoredCount = 0;
        int skippedAlreadyScoredCount = 0;

        for (CollectedItem candidate : candidates) {
            if (rankingScoreRepository.existsByCollectedItemId(candidate.getId())) {
                skippedAlreadyScoredCount++;
                continue;
            }

            rankingScoreRepository.save(score(candidate));
            scoredCount++;
        }

        return new RankingRunResult(candidates.size(), scoredCount, skippedAlreadyScoredCount);
    }

    @Transactional(readOnly = true)
    public List<RankingScoreResponse> findTopRankings(int limit) {
        int effectiveLimit = Math.clamp(limit, 1, 10);
        return rankingScoreRepository.findAllByOrderByTotalScoreDescScoredAtDesc(PageRequest.of(0, effectiveLimit))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RankingScore score(CollectedItem item) {
        String text = searchableText(item);
        ScoreAccumulator accumulator = new ScoreAccumulator();

        applyRules(text, POSITIVE_RULES, accumulator);
        applyRules(text, NEGATIVE_RULES, accumulator);

        return new RankingScore(
                item,
                accumulator.backendRelevanceScore(),
                accumulator.learningValueScore(),
                accumulator.implementationValueScore(),
                accumulator.reasons()
        );
    }

    private void applyRules(String text, List<KeywordScoreRule> rules, ScoreAccumulator accumulator) {
        for (KeywordScoreRule rule : rules) {
            if (text.contains(rule.keyword())) {
                accumulator.add(rule);
            }
        }
    }

    private String searchableText(CollectedItem item) {
        return String.join(" ",
                nullToEmpty(item.getTitle()),
                nullToEmpty(item.getSummary()),
                nullToEmpty(item.getCanonicalUrl()),
                item.getType().name()
        ).toLowerCase(Locale.ROOT);
    }

    private RankingScoreResponse toResponse(RankingScore score) {
        CollectedItem item = score.getCollectedItem();
        return new RankingScoreResponse(
                score.getId(),
                item.getId(),
                item.getTitle(),
                item.getSummary(),
                item.getCanonicalUrl(),
                score.getBackendRelevanceScore(),
                score.getLearningValueScore(),
                score.getImplementationValueScore(),
                score.getTotalScore(),
                score.getReasons()
        );
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static class ScoreAccumulator {

        private int backendRelevanceScore;
        private int learningValueScore;
        private int implementationValueScore;
        private final List<String> reasons = new ArrayList<>();

        void add(KeywordScoreRule rule) {
            backendRelevanceScore += rule.backendRelevanceScore();
            learningValueScore += rule.learningValueScore();
            implementationValueScore += rule.implementationValueScore();
            reasons.add(rule.reason());
        }

        int backendRelevanceScore() {
            return Math.max(backendRelevanceScore, 0);
        }

        int learningValueScore() {
            return Math.max(learningValueScore, 0);
        }

        int implementationValueScore() {
            return Math.max(implementationValueScore, 0);
        }

        String reasons() {
            if (reasons.isEmpty()) {
                return "No matching backend relevance rules";
            }
            return String.join(", ", reasons);
        }
    }
}
