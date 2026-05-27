package com.questack.ranking.api;

import com.questack.ranking.api.dto.RankingRunResult;
import com.questack.ranking.api.dto.RankingScoreResponse;
import com.questack.ranking.service.RankingService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @PostMapping("/rankings")
    public RankingRunResult rankCollectedItems() {
        return rankingService.rankUnscoredItems();
    }

    @GetMapping("/rankings/top")
    public List<RankingScoreResponse> findTopRankings(
            @RequestParam(defaultValue = "3") @Min(1) @Max(10) int limit
    ) {
        return rankingService.findTopRankings(limit);
    }
}
