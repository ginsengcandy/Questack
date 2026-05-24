package com.questack.collection.github.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github")
public record GithubProperties(
        String token,
        String apiBaseUrl,
        String searchRepositoriesPath
) {
}
