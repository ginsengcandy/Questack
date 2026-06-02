package com.questack.quest.generator;

import static org.assertj.core.api.Assertions.assertThat;

import com.questack.quest.template.MiniProjectQuestTemplates;
import org.junit.jupiter.api.Test;

class MiniProjectSkeletonGeneratorTest {

    private final MiniProjectSkeletonGenerator generator = new MiniProjectSkeletonGenerator();

    @Test
    void generatesDeterministicRunnableSkeletonFiles() {
        GeneratedQuestProject project = generator.generate(MiniProjectQuestTemplates.redisCacheInvalidation());

        assertThat(project.projectSlug()).isEqualTo("redis-cache-invalidation");
        assertThat(project.files())
                .extracting(GeneratedQuestFile::path)
                .containsExactly(
                        "redis-cache-invalidation/README.md",
                        "redis-cache-invalidation/acceptance-criteria.md",
                        "redis-cache-invalidation/build.gradle",
                        "redis-cache-invalidation/src/main/java/quest/RedisCacheInvalidationQuest.java",
                        "redis-cache-invalidation/src/test/java/quest/RedisCacheInvalidationQuestTest.java"
                );
    }

    @Test
    void readmeAndAcceptanceCriteriaCarryQuestContract() {
        GeneratedQuestProject project = generator.generate(MiniProjectQuestTemplates.jwtRefreshTokenRotation());

        assertThat(project.file("jwt-refresh-token-rotation/README.md").content())
                .contains("# Mini Project Quest: JWT refresh token rotation")
                .contains("## TODO-STUDENT Boundaries")
                .contains("TODO-STUDENT: implement-refresh-token-reuse-detection")
                .contains("Implement only the sections marked with `TODO-STUDENT`.");
        assertThat(project.file("jwt-refresh-token-rotation/acceptance-criteria.md").content())
                .contains("- [ ] Refreshing a valid token issues a new refresh token and invalidates the old one.")
                .contains("- [ ] Auth API with login and refresh endpoints.");
    }

    @Test
    void starterCodeLeavesLearningCriticalLogicUnimplemented() {
        GeneratedQuestProject project = generator.generate(MiniProjectQuestTemplates.kafkaRetryDeadLetterQueue());
        String starterCode = project.file(
                "kafka-retry-dead-letter-queue/src/main/java/quest/KafkaRetryDeadLetterQueueQuest.java"
        ).content();

        assertThat(starterCode)
                .contains("TODO-STUDENT: implement-idempotent-consumer-and-dlq-routing")
                .contains("throw new UnsupportedOperationException")
                .doesNotContain("return true;");
    }

    @Test
    void testSkeletonPointsStudentAtTodoBoundaryWithoutSolvingIt() {
        GeneratedQuestProject project = generator.generate(MiniProjectQuestTemplates.springAiRagDocumentSearch());
        String testSkeleton = project.file(
                "spring-ai-rag-document-search/src/test/java/quest/SpringAiRagDocumentSearchQuestTest.java"
        ).content();

        assertThat(testSkeleton)
                .contains("learningCriticalLogicIsLeftForTheStudent")
                .contains("TODO-STUDENT: implement-rag-context-assembly")
                .contains("Fill this test with the expected behavior before implementing the TODO-STUDENT code.")
                .doesNotContain("completeLearningCriticalLogic()).isTrue()");
    }
}
