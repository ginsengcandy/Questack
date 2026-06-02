package com.questack.quest.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MiniProjectQuestTemplateTest {

    private final MiniProjectQuestTemplateRenderer renderer = new MiniProjectQuestTemplateRenderer();

    @Test
    void exampleTemplatesCoverExpectedBackendQuestTopics() {
        assertThat(MiniProjectQuestTemplates.examples())
                .extracting(MiniProjectQuestTemplate::templateId)
                .containsExactly(
                        "redis-cache-invalidation",
                        "jwt-refresh-token-rotation",
                        "kafka-retry-dead-letter-queue",
                        "spring-ai-rag-document-search"
                );
    }

    @Test
    void everyExampleTemplateDefinesStableQuestSectionsAndTodoStudentBoundaries() {
        assertThat(MiniProjectQuestTemplates.examples())
                .allSatisfy(template -> {
                    assertThat(template.topic()).isNotBlank();
                    assertThat(template.scenario()).isNotBlank();
                    assertThat(template.difficulty()).isEqualTo("junior-backend-interview");
                    assertThat(template.learningGoals()).hasSizeGreaterThanOrEqualTo(3);
                    assertThat(template.requiredConcepts()).hasSizeGreaterThanOrEqualTo(3);
                    assertThat(template.acceptanceCriteria()).hasSizeGreaterThanOrEqualTo(3);
                    assertThat(template.deliverables()).hasSizeGreaterThanOrEqualTo(3);
                    assertThat(template.todoStudentBoundaries()).isNotEmpty();
                    assertThat(template.todoStudentBoundaries())
                            .allSatisfy(boundary -> {
                                assertThat(boundary.marker()).startsWith("TODO-STUDENT");
                                assertThat(boundary.filePath()).isNotBlank();
                                assertThat(boundary.responsibility()).isNotBlank();
                                assertThat(boundary.successHint()).isNotBlank();
                            });
                });
    }

    @Test
    void rendersMarkdownQuestFormatWithTodoStudentSection() {
        String markdown = renderer.renderMarkdown(MiniProjectQuestTemplates.kafkaRetryDeadLetterQueue());

        assertThat(markdown).contains("# Mini Project Quest: Kafka consumer retry and dead-letter queue");
        assertThat(markdown).contains("## Scenario");
        assertThat(markdown).contains("## Learning Goals");
        assertThat(markdown).contains("## Required Concepts");
        assertThat(markdown).contains("## Acceptance Criteria");
        assertThat(markdown).contains("## Expected Deliverables");
        assertThat(markdown).contains("## TODO-STUDENT Boundaries");
        assertThat(markdown).contains("TODO-STUDENT: implement-idempotent-consumer-and-dlq-routing");
        assertThat(markdown).contains("OrderEventConsumer.java");
    }

    @Test
    void rejectsTemplatesWithoutTodoStudentBoundaries() {
        assertThatThrownBy(() -> new MiniProjectQuestTemplate(
                "invalid",
                "Invalid quest",
                "Missing learning-critical TODO boundary.",
                "junior-backend-interview",
                java.util.List.of("Learn something"),
                java.util.List.of("Concept"),
                java.util.List.of("Criterion"),
                java.util.List.of("Deliverable"),
                java.util.List.of()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TODO-STUDENT");
    }

    @Test
    void rejectsTodoBoundariesWithoutTodoStudentMarker() {
        assertThatThrownBy(() -> new TodoStudentBoundary(
                "TODO: implement important logic",
                "Example.java",
                "Implement logic.",
                "Test passes."
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TODO-STUDENT");
    }
}
