package com.questack.source;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "sources")
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SourceType type;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Source() {
    }

    public Source(String name, SourceType type, String url, int priority) {
        this.name = name;
        this.type = type;
        this.url = url;
        this.priority = priority;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SourceType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public boolean isActive() {
        return active;
    }

    public int getPriority() {
        return priority;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void deactivate() {
        this.active = false;
    }
}
