# Mini Project Quest Template

Questack mini project quests turn one selected backend learning topic into a small hands-on project.
The template is intentionally deterministic for the MVP. It defines the format before a generator creates project skeleton files.

## Required Fields

- `templateId`: stable identifier for the quest template
- `topic`: learner-facing project topic
- `scenario`: short backend scenario the project reproduces
- `difficulty`: currently fixed to `junior-backend-interview`
- `learningGoals`: concepts the learner should be able to explain after finishing
- `requiredConcepts`: prerequisite keywords and implementation concepts
- `acceptanceCriteria`: observable behavior required for completion
- `deliverables`: files, tests, and documentation the learner should produce
- `todoStudentBoundaries`: learning-critical implementation boundaries that must be left to the learner

## TODO-STUDENT Rule

Every template must include at least one `TODO-STUDENT` boundary.

The boundary names the file, the learner responsibility, and the success hint. Questack can generate surrounding scaffolding later, but the core learning logic must remain unfinished.

Example:

```text
TODO-STUDENT: implement-cache-invalidation
file: src/main/java/.../ProductCacheService.java
responsibility: Implement the cache eviction or refresh policy after product updates.
success hint: A stale cache test fails before this logic exists and passes after the fix.
```

## Example Template Topics

- Redis cache invalidation in Spring Boot
- JWT refresh token rotation
- Kafka consumer retry and dead-letter queue
- Spring AI RAG document search

## Markdown Rendering

Rendered quest Markdown uses these sections:

1. Scenario
2. Learning Goals
3. Required Concepts
4. Acceptance Criteria
5. Expected Deliverables
6. TODO-STUDENT Boundaries

This format is the contract for the later mini project skeleton generator.

## Skeleton Generator Contract

The skeleton generator turns one `MiniProjectQuestTemplate` into a deterministic project file set:

- `build.gradle`
- `README.md`
- `acceptance-criteria.md`
- `src/main/java/quest/{TemplateClassName}Quest.java`
- `src/test/java/quest/{TemplateClassName}QuestTest.java`

The generated starter code must leave learning-critical behavior unfinished with `TODO-STUDENT` and an `UnsupportedOperationException`.
The generated test skeleton may point to the TODO boundary, but it must not solve the learning-critical behavior for the student.
