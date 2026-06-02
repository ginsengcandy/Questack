package com.questack.quest.template;

import java.util.List;

public final class MiniProjectQuestTemplates {

    private static final String JUNIOR_BACKEND_DIFFICULTY = "junior-backend-interview";

    private MiniProjectQuestTemplates() {
    }

    public static List<MiniProjectQuestTemplate> examples() {
        return List.of(
                redisCacheInvalidation(),
                jwtRefreshTokenRotation(),
                kafkaRetryDeadLetterQueue(),
                springAiRagDocumentSearch()
        );
    }

    public static MiniProjectQuestTemplate redisCacheInvalidation() {
        return new MiniProjectQuestTemplate(
                "redis-cache-invalidation",
                "Redis cache invalidation in Spring Boot",
                "Build a product lookup API that caches product details in Redis and invalidates stale cache entries after product updates.",
                JUNIOR_BACKEND_DIFFICULTY,
                List.of(
                        "Explain cache-aside flow and stale data risk.",
                        "Implement cache lookup, fallback, write-through invalidation, and observable cache decisions.",
                        "Write tests for cache hit, cache miss, update, and stale cache prevention."
                ),
                List.of("Redis", "cache-aside", "TTL", "invalidation", "Spring Cache", "service-layer testing"),
                List.of(
                        "Product lookup returns cached data after the first successful database read.",
                        "Product update invalidates or refreshes the corresponding cache key.",
                        "Tests prove that stale data is not served after an update."
                ),
                List.of(
                        "Spring Boot API with product read/update endpoints.",
                        "README explaining cache trade-offs and invalidation policy.",
                        "Unit or integration tests for cache behavior."
                ),
                List.of(new TodoStudentBoundary(
                        "TODO-STUDENT: implement-cache-invalidation",
                        "src/main/java/.../ProductCacheService.java",
                        "Implement the cache eviction or refresh policy after product updates.",
                        "A test fails before this logic exists and passes after stale cache entries are removed."
                ))
        );
    }

    public static MiniProjectQuestTemplate jwtRefreshTokenRotation() {
        return new MiniProjectQuestTemplate(
                "jwt-refresh-token-rotation",
                "JWT refresh token rotation",
                "Build login and token refresh APIs that rotate refresh tokens and detect token reuse.",
                JUNIOR_BACKEND_DIFFICULTY,
                List.of(
                        "Explain access token and refresh token responsibilities.",
                        "Model refresh token rotation, reuse detection, and session revocation.",
                        "Test normal refresh, expired token, and reuse attack scenarios."
                ),
                List.of("JWT", "refresh token", "rotation", "reuse detection", "revocation", "Spring Security"),
                List.of(
                        "Refreshing a valid token issues a new refresh token and invalidates the old one.",
                        "Reusing an old refresh token revokes the token family or session.",
                        "Security-sensitive events are logged without exposing secrets."
                ),
                List.of(
                        "Auth API with login and refresh endpoints.",
                        "Token storage model with rotation state.",
                        "Tests for success and token reuse detection."
                ),
                List.of(new TodoStudentBoundary(
                        "TODO-STUDENT: implement-refresh-token-reuse-detection",
                        "src/main/java/.../RefreshTokenService.java",
                        "Detect reused refresh tokens and revoke the affected session.",
                        "A reuse attack test fails before this logic exists and passes after revocation is enforced."
                ))
        );
    }

    public static MiniProjectQuestTemplate kafkaRetryDeadLetterQueue() {
        return new MiniProjectQuestTemplate(
                "kafka-retry-dead-letter-queue",
                "Kafka consumer retry and dead-letter queue",
                "Build an order event consumer that retries transient failures and routes poison messages to a dead-letter queue.",
                JUNIOR_BACKEND_DIFFICULTY,
                List.of(
                        "Explain at-least-once processing and idempotency.",
                        "Separate retryable failures from poison message failures.",
                        "Write tests for duplicate event handling and dead-letter routing."
                ),
                List.of("Kafka", "consumer retry", "DLQ", "idempotency", "event-driven backend", "failure isolation"),
                List.of(
                        "The consumer records processed event IDs to avoid duplicate side effects.",
                        "Retryable failures are retried with a bounded policy.",
                        "Poison messages are sent to a dead-letter queue with enough context to investigate."
                ),
                List.of(
                        "Order event consumer module.",
                        "Failure handling policy documented in README.",
                        "Tests for retry, duplicate event, and DLQ behavior."
                ),
                List.of(new TodoStudentBoundary(
                        "TODO-STUDENT: implement-idempotent-consumer-and-dlq-routing",
                        "src/main/java/.../OrderEventConsumer.java",
                        "Implement duplicate event protection and dead-letter routing for unrecoverable messages.",
                        "Duplicate event and poison message tests pass without producing repeated side effects."
                ))
        );
    }

    public static MiniProjectQuestTemplate springAiRagDocumentSearch() {
        return new MiniProjectQuestTemplate(
                "spring-ai-rag-document-search",
                "Spring AI RAG document search",
                "Build a document question-answering API that retrieves relevant document snippets before generating an answer draft.",
                JUNIOR_BACKEND_DIFFICULTY,
                List.of(
                        "Explain retrieval-augmented generation and context quality.",
                        "Design document ingestion, retrieval, prompt assembly, and fallback boundaries.",
                        "Test retrieval quality and LLM failure fallback without relying on a live LLM call."
                ),
                List.of("Spring AI", "RAG", "embedding", "retrieval", "prompt context", "fallback"),
                List.of(
                        "The API retrieves relevant snippets before answer generation.",
                        "Prompt context includes citations or source identifiers.",
                        "Fallback behavior is defined for empty retrieval results or model failure."
                ),
                List.of(
                        "Question-answering API with in-memory fixture documents.",
                        "README explaining retrieval and fallback strategy.",
                        "Tests for retrieval match, empty result, and fallback response."
                ),
                List.of(new TodoStudentBoundary(
                        "TODO-STUDENT: implement-rag-context-assembly",
                        "src/main/java/.../RagAnswerService.java",
                        "Assemble retrieved snippets into a bounded prompt context and define fallback behavior.",
                        "Tests prove that relevant snippets are included and empty retrieval returns the documented fallback."
                ))
        );
    }
}
