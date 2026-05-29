package com.questack.ranking.api.dto;

public record RankingScoreResponse(
        Long rankingScoreId,
        Long collectedItemId,
        String title,
        String summary,
        String canonicalUrl,
        int backendRelevanceScore,
        int learningValueScore,
        int implementationValueScore,
        int totalScore,
        String reasons
) {
}
