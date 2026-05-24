package com.questack.collection.github.client;

import com.questack.collection.github.client.dto.GithubRepositoryItem;
import com.questack.collection.github.client.dto.GithubRepositorySearchResponse;
import com.questack.collection.github.config.GithubProperties;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GithubSearchClient {

    private static final String DEFAULT_QUERY = "language:Java topic:spring-boot stars:>=100 fork:false";

    private final GithubProperties properties;
    private final RestClient restClient;

    public GithubSearchClient(GithubProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "Questack")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    public List<GithubRepositoryItem> searchRepositories(String query, int perPage) {
        String effectiveQuery = StringUtils.hasText(query) ? query : DEFAULT_QUERY;
        int effectivePerPage = Math.clamp(perPage, 1, 30);
        String url = UriComponentsBuilder.fromUriString(properties.apiBaseUrl())
                .path(properties.searchRepositoriesPath())
                .queryParam("q", effectiveQuery)
                .queryParam("sort", "updated")
                .queryParam("order", "desc")
                .queryParam("per_page", effectivePerPage)
                .build()
                .toUriString();

        GithubRepositorySearchResponse response = restClient.get()
                .uri(url)
                .headers(this::applyAuthHeader)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(GithubRepositorySearchResponse.class);

        if (response == null || response.items() == null) {
            return List.of();
        }
        return response.items();
    }

    private void applyAuthHeader(HttpHeaders headers) {
        if (StringUtils.hasText(properties.token())) {
            headers.setBearerAuth(properties.token());
        }
    }
}
