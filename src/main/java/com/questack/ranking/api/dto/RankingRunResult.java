package com.questack.ranking.api.dto;

public record RankingRunResult(
        int candidateCount,
        int scoredCount,
        int skippedAlreadyScoredCount
) {
}
