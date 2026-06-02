package com.questack.quest.template;

public record TodoStudentBoundary(
        String marker,
        String filePath,
        String responsibility,
        String successHint
) {

    public TodoStudentBoundary {
        if (!marker.startsWith("TODO-STUDENT")) {
            throw new IllegalArgumentException("TODO-STUDENT boundary marker must start with TODO-STUDENT.");
        }
    }
}
