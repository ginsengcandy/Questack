package com.questack.collection.github.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record GithubRepositoryItem(
        long id,
        String name,
        @JsonProperty("full_name") String fullName,
        @JsonProperty("html_url") String htmlUrl,
        String description,
        @JsonProperty("stargazers_count") int stargazersCount,
        String language,
        @JsonProperty("updated_at") Instant updatedAt,
        GithubOwner owner
) {
}
