package com.questack.quest.template;

import java.util.List;

public record MiniProjectQuestTemplate(
        String templateId,
        String topic,
        String scenario,
        String difficulty,
        List<String> learningGoals,
        List<String> requiredConcepts,
        List<String> acceptanceCriteria,
        List<String> deliverables,
        List<TodoStudentBoundary> todoStudentBoundaries
) {

    public MiniProjectQuestTemplate {
        learningGoals = List.copyOf(learningGoals);
        requiredConcepts = List.copyOf(requiredConcepts);
        acceptanceCriteria = List.copyOf(acceptanceCriteria);
        deliverables = List.copyOf(deliverables);
        todoStudentBoundaries = List.copyOf(todoStudentBoundaries);

        if (todoStudentBoundaries.isEmpty()) {
            throw new IllegalArgumentException("Mini project quest template must include TODO-STUDENT boundaries.");
        }
    }
}
