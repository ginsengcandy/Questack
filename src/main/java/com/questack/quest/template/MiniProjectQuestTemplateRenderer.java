package com.questack.quest.template;

import java.util.List;

public class MiniProjectQuestTemplateRenderer {

    public String renderMarkdown(MiniProjectQuestTemplate template) {
        StringBuilder builder = new StringBuilder();
        builder.append("# Mini Project Quest: ").append(template.topic()).append(System.lineSeparator());
        builder.append(System.lineSeparator());
        builder.append("- Template ID: ").append(template.templateId()).append(System.lineSeparator());
        builder.append("- Difficulty: ").append(template.difficulty()).append(System.lineSeparator());
        builder.append(System.lineSeparator());
        builder.append("## Scenario").append(System.lineSeparator());
        builder.append(template.scenario()).append(System.lineSeparator());
        builder.append(System.lineSeparator());
        appendList(builder, "Learning Goals", template.learningGoals());
        appendList(builder, "Required Concepts", template.requiredConcepts());
        appendList(builder, "Acceptance Criteria", template.acceptanceCriteria());
        appendList(builder, "Expected Deliverables", template.deliverables());
        appendTodoStudentBoundaries(builder, template.todoStudentBoundaries());
        return builder.toString();
    }

    private void appendList(StringBuilder builder, String title, List<String> values) {
        builder.append("## ").append(title).append(System.lineSeparator());
        for (String value : values) {
            builder.append("- ").append(value).append(System.lineSeparator());
        }
        builder.append(System.lineSeparator());
    }

    private void appendTodoStudentBoundaries(StringBuilder builder, List<TodoStudentBoundary> boundaries) {
        builder.append("## TODO-STUDENT Boundaries").append(System.lineSeparator());
        for (TodoStudentBoundary boundary : boundaries) {
            builder.append("- `").append(boundary.marker()).append("` in `").append(boundary.filePath()).append("`: ")
                    .append(boundary.responsibility())
                    .append(" Success hint: ")
                    .append(boundary.successHint())
                    .append(System.lineSeparator());
        }
        builder.append(System.lineSeparator());
    }
}
