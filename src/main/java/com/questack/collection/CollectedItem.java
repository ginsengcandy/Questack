package com.questack.collection;

import com.questack.source.Source;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
        name = "collected_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_collected_items_canonical_url", columnNames = "canonical_url")
        }
)
public class CollectedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CollectedItemType type;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 2000)
    private String summary;

    @Column(name = "canonical_url", nullable = false, length = 1000)
    private String canonicalUrl;

    @Column(length = 100)
    private String externalId;

    @Column(length = 100)
    private String author;

    private Instant publishedAt;

    @Column(nullable = false)
    private Instant collectedAt = Instant.now();

    protected CollectedItem() {
    }

    public CollectedItem(
            Source source,
            CollectedItemType type,
            String title,
            String summary,
            String canonicalUrl,
            String externalId,
            String author,
            Instant publishedAt
    ) {
        this.source = source;
        this.type = type;
        this.title = title;
        this.summary = summary;
        this.canonicalUrl = canonicalUrl;
        this.externalId = externalId;
        this.author = author;
        this.publishedAt = publishedAt;
    }

    public Long getId() {
        return id;
    }

    public Source getSource() {
        return source;
    }

    public CollectedItemType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getAuthor() {
        return author;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }
}
