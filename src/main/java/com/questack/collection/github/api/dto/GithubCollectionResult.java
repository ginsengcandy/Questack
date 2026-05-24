package com.questack.collection.github.api.dto;

public record GithubCollectionResult(
        int fetchedCount,
        int savedCount,
        int skippedDuplicateCount
) {
}
