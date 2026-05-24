package com.questack.collection;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectedItemRepository extends JpaRepository<CollectedItem, Long> {

    boolean existsByCanonicalUrl(String canonicalUrl);

    Optional<CollectedItem> findByCanonicalUrl(String canonicalUrl);

    List<CollectedItem> findByCollectedAtAfterOrderByCollectedAtDesc(Instant collectedAfter);
}
