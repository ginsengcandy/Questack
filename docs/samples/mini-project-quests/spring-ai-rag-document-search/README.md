# Mini Project Quest: Spring AI RAG document search

> 샘플 파일입니다. `MiniProjectSkeletonGenerator`가 만드는 deterministic file set의 포트폴리오 예시이며, 학습 핵심 로직은 의도적으로 `TODO-STUDENT`로 남겨둡니다.

- Template ID: spring-ai-rag-document-search
- Difficulty: junior-backend-interview

## Scenario
Build a document question-answering API that retrieves relevant document snippets before generating an answer draft.

## Learning Goals
- Explain retrieval-augmented generation and context quality.
- Design document ingestion, retrieval, prompt assembly, and fallback boundaries.
- Test retrieval quality and LLM failure fallback without relying on a live LLM call.

## Required Concepts
- Spring AI
- RAG
- embedding
- retrieval
- prompt context
- fallback

## Acceptance Criteria
- The API retrieves relevant snippets before answer generation.
- Prompt context includes citations or source identifiers.
- Fallback behavior is defined for empty retrieval results or model failure.

## Expected Deliverables
- Question-answering API with in-memory fixture documents.
- README explaining retrieval and fallback strategy.
- Tests for retrieval match, empty result, and fallback response.

## TODO-STUDENT Boundaries
- `TODO-STUDENT: implement-rag-context-assembly` in `src/main/java/.../RagAnswerService.java`: Assemble retrieved snippets into a bounded prompt context and define fallback behavior. Success hint: Tests prove that relevant snippets are included and empty retrieval returns the documented fallback.

## How To Use This Skeleton
- Implement only the sections marked with `TODO-STUDENT`.
- Keep generated scaffolding intact unless a test requires a small supporting change.
- Run the project tests and update this README with the trade-offs you learned.
