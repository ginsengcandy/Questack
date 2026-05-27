package com.questack.ranking;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankingScoreRepository extends JpaRepository<RankingScore, Long> {

    boolean existsByCollectedItemId(Long collectedItemId);

    Optional<RankingScore> findByCollectedItemId(Long collectedItemId);

    List<RankingScore> findTop3ByOrderByTotalScoreDescScoredAtDesc();

    List<RankingScore> findAllByOrderByTotalScoreDescScoredAtDesc(Pageable pageable);
}
