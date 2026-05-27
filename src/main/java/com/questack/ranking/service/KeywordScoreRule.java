package com.questack.ranking.service;

record KeywordScoreRule(
        String keyword,
        int backendRelevanceScore,
        int learningValueScore,
        int implementationValueScore,
        String reason
) {
}
