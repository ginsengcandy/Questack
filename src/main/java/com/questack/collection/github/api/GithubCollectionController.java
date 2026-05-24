package com.questack.collection.github.api;

import com.questack.collection.github.api.dto.GithubCollectionResult;
import com.questack.collection.github.service.GithubCollector;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class GithubCollectionController {

    private final GithubCollector githubCollector;

    public GithubCollectionController(GithubCollector githubCollector) {
        this.githubCollector = githubCollector;
    }

    @PostMapping("/collections/github")
    public GithubCollectionResult collectGithubRepositories(
            @RequestParam(defaultValue = "language:Java topic:spring-boot stars:>=100 fork:false") String query,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) int perPage
    ) {
        return githubCollector.collect(query, perPage);
    }
}
