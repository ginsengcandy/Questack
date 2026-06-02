package com.questack.quest.generator;

import java.util.Comparator;
import java.util.List;

public record GeneratedQuestProject(
        String projectSlug,
        List<GeneratedQuestFile> files
) {

    public GeneratedQuestProject {
        files = files.stream()
                .sorted(Comparator.comparing(GeneratedQuestFile::path))
                .toList();
    }

    public GeneratedQuestFile file(String path) {
        return files.stream()
                .filter(file -> file.path().equals(path))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Generated file not found: " + path));
    }
}
