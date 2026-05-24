package com.questack.collection.github.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GithubRepositorySearchResponse(
        @JsonProperty("total_count") int totalCount,
        @JsonProperty("incomplete_results") boolean incompleteResults,
        List<GithubRepositoryItem> items
) {
}
