package com.questack.ranking;

import com.questack.collection.CollectedItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ranking_scores")
public class RankingScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collected_item_id", nullable = false, unique = true)
    private CollectedItem collectedItem;

    @Column(nullable = false)
    private int backendRelevanceScore;

    @Column(nullable = false)
    private int learningValueScore;

    @Column(nullable = false)
    private int implementationValueScore;

    @Column(nullable = false)
    private int totalScore;

    @Column(length = 1000)
    private String reasons;

    @Column(nullable = false)
    private Instant scoredAt = Instant.now();

    protected RankingScore() {
    }

    public RankingScore(
            CollectedItem collectedItem,
            int backendRelevanceScore,
            int learningValueScore,
            int implementationValueScore,
            String reasons
    ) {
        this.collectedItem = collectedItem;
        this.backendRelevanceScore = backendRelevanceScore;
        this.learningValueScore = learningValueScore;
        this.implementationValueScore = implementationValueScore;
        this.totalScore = backendRelevanceScore + learningValueScore + implementationValueScore;
        this.reasons = reasons;
    }

    public Long getId() {
        return id;
    }

    public CollectedItem getCollectedItem() {
        return collectedItem;
    }

    public int getBackendRelevanceScore() {
        return backendRelevanceScore;
    }

    public int getLearningValueScore() {
        return learningValueScore;
    }

    public int getImplementationValueScore() {
        return implementationValueScore;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public String getReasons() {
        return reasons;
    }

    public Instant getScoredAt() {
        return scoredAt;
    }
}
