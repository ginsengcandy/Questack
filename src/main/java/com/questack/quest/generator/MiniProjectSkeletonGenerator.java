package com.questack.quest.generator;

import com.questack.quest.template.MiniProjectQuestTemplate;
import com.questack.quest.template.MiniProjectQuestTemplateRenderer;
import com.questack.quest.template.TodoStudentBoundary;
import java.util.ArrayList;
import java.util.List;

public class MiniProjectSkeletonGenerator {

    private final MiniProjectQuestTemplateRenderer templateRenderer = new MiniProjectQuestTemplateRenderer();

    public GeneratedQuestProject generate(MiniProjectQuestTemplate template) {
        String projectSlug = template.templateId();
        List<GeneratedQuestFile> files = new ArrayList<>();
        files.add(new GeneratedQuestFile(projectSlug + "/build.gradle", buildGradle()));
        files.add(new GeneratedQuestFile(projectSlug + "/README.md", readme(template)));
        files.add(new GeneratedQuestFile(projectSlug + "/acceptance-criteria.md", acceptanceCriteria(template)));
        files.add(new GeneratedQuestFile(projectSlug + "/src/main/java/quest/" + className(template) + ".java", starterCode(template)));
        files.add(new GeneratedQuestFile(projectSlug + "/src/test/java/quest/" + className(template) + "Test.java", testSkeleton(template)));
        return new GeneratedQuestProject(projectSlug, files);
    }

    private String readme(MiniProjectQuestTemplate template) {
        return templateRenderer.renderMarkdown(template)
                + "## How To Use This Skeleton" + System.lineSeparator()
                + "- Implement only the sections marked with `TODO-STUDENT`." + System.lineSeparator()
                + "- Keep generated scaffolding intact unless a test requires a small supporting change." + System.lineSeparator()
                + "- Run the project tests and update this README with the trade-offs you learned." + System.lineSeparator();
    }

    private String buildGradle() {
        return """
                plugins {
                    id 'java'
                }

                repositories {
                    mavenCentral()
                }

                dependencies {
                    testImplementation 'org.junit.jupiter:junit-jupiter:5.12.2'
                    testImplementation 'org.assertj:assertj-core:3.27.3'
                }

                test {
                    useJUnitPlatform()
                }
                """;
    }

    private String acceptanceCriteria(MiniProjectQuestTemplate template) {
        StringBuilder builder = new StringBuilder();
        builder.append("# Acceptance Criteria").append(System.lineSeparator());
        builder.append(System.lineSeparator());
        for (String criterion : template.acceptanceCriteria()) {
            builder.append("- [ ] ").append(criterion).append(System.lineSeparator());
        }
        builder.append(System.lineSeparator());
        builder.append("## Expected Deliverables").append(System.lineSeparator());
        for (String deliverable : template.deliverables()) {
            builder.append("- [ ] ").append(deliverable).append(System.lineSeparator());
        }
        return builder.toString();
    }

    private String starterCode(MiniProjectQuestTemplate template) {
        String className = className(template);
        TodoStudentBoundary boundary = template.todoStudentBoundaries().getFirst();
        return """
                package quest;

                public class %s {

                    public String describeQuest() {
                        return "%s";
                    }

                    public boolean completeLearningCriticalLogic() {
                        // %s
                        // Responsibility: %s
                        // Success hint: %s
                        throw new UnsupportedOperationException("TODO-STUDENT: implement the learning-critical logic.");
                    }
                }
                """.formatted(
                className,
                escapeJava(template.topic()),
                boundary.marker(),
                escapeJava(boundary.responsibility()),
                escapeJava(boundary.successHint())
        );
    }

    private String testSkeleton(MiniProjectQuestTemplate template) {
        String className = className(template);
        TodoStudentBoundary boundary = template.todoStudentBoundaries().getFirst();
        return """
                package quest;

                import static org.assertj.core.api.Assertions.assertThat;

                import org.junit.jupiter.api.Test;

                class %sTest {

                    @Test
                    void describesQuestTopic() {
                        %s subject = new %s();

                        assertThat(subject.describeQuest()).isEqualTo("%s");
                    }

                    @Test
                    void learningCriticalLogicIsLeftForTheStudent() {
                        // %s
                        // Fill this test with the expected behavior before implementing the TODO-STUDENT code.
                        assertThat("%s").startsWith("TODO-STUDENT");
                    }
                }
                """.formatted(
                className,
                className,
                className,
                escapeJava(template.topic()),
                boundary.marker(),
                boundary.marker()
        );
    }

    private String className(MiniProjectQuestTemplate template) {
        StringBuilder builder = new StringBuilder();
        for (String part : template.templateId().split("-")) {
            if (part.isBlank()) {
                continue;
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        builder.append("Quest");
        return builder.toString();
    }

    private String escapeJava(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
