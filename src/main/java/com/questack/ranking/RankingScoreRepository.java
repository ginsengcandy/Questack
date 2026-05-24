package com.questack.ranking;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankingScoreRepository extends JpaRepository<RankingScore, Long> {

    Optional<RankingScore> findByCollectedItemId(Long collectedItemId);

    List<RankingScore> findTop3ByOrderByTotalScoreDescScoredAtDesc();
}
