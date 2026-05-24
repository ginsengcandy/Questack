package com.questack.collection.github.service;

import com.questack.collection.CollectedItem;
import com.questack.collection.CollectedItemRepository;
import com.questack.collection.CollectedItemType;
import com.questack.collection.github.api.dto.GithubCollectionResult;
import com.questack.collection.github.client.GithubSearchClient;
import com.questack.collection.github.client.dto.GithubRepositoryItem;
import com.questack.source.Source;
import com.questack.source.SourceRepository;
import com.questack.source.SourceType;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GithubCollector {

    private static final String SOURCE_NAME = "GitHub Search";
    private static final String SOURCE_URL = "https://github.com/search";

    private final GithubSearchClient githubSearchClient;
    private final SourceRepository sourceRepository;
    private final CollectedItemRepository collectedItemRepository;

    public GithubCollector(
            GithubSearchClient githubSearchClient,
            SourceRepository sourceRepository,
            CollectedItemRepository collectedItemRepository
    ) {
        this.githubSearchClient = githubSearchClient;
        this.sourceRepository = sourceRepository;
        this.collectedItemRepository = collectedItemRepository;
    }

    @Transactional
    public GithubCollectionResult collect(String query, int perPage) {
        Source source = sourceRepository.findByName(SOURCE_NAME)
                .orElseGet(() -> sourceRepository.save(new Source(SOURCE_NAME, SourceType.GITHUB, SOURCE_URL, 1)));

        List<GithubRepositoryItem> repositories = githubSearchClient.searchRepositories(query, perPage);
        int savedCount = 0;
        int skippedDuplicateCount = 0;

        for (GithubRepositoryItem repository : repositories) {
            if (collectedItemRepository.existsByCanonicalUrl(repository.htmlUrl())) {
                skippedDuplicateCount++;
                continue;
            }

            CollectedItem item = new CollectedItem(
                    source,
                    CollectedItemType.GITHUB_REPOSITORY,
                    repository.fullName(),
                    buildSummary(repository),
                    repository.htmlUrl(),
                    String.valueOf(repository.id()),
                    repository.owner() == null ? null : repository.owner().login(),
                    repository.updatedAt()
            );
            collectedItemRepository.save(item);
            savedCount++;
        }

        return new GithubCollectionResult(repositories.size(), savedCount, skippedDuplicateCount);
    }

    private String buildSummary(GithubRepositoryItem repository) {
        return "language=%s, stars=%d, description=%s".formatted(
                repository.language(),
                repository.stargazersCount(),
                repository.description()
        );
    }
}
