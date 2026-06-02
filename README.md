# Questack

Korean translation: [README.ko.md](README.ko.md). `README.md` is the single source of truth; the Korean version is a translation that should be updated whenever this file changes.

Questack is a backend-focused study automation service.

The name means **Quest + Stack**. The product idea is simple: collect backend and AI engineering signals every day, filter them for Java/Spring/backend relevance, and turn the useful ones into short learning quests and mini-projects.

This repository is intentionally built as a small, inspectable Spring Boot backend rather than a feature-heavy prototype. The MVP focuses on a reliable learning pipeline: external source integration, normalized persistence, duplicate handling, ranking, daily briefing output, and mini-project quest generation.

## Current Scope

Implemented:

- Spring Boot 3.5.x application on Java 21
- H2-backed local persistence
- GitHub Search API repository collection
- RSS/Atom technical blog collection
- Normalized source and collected item model
- Duplicate prevention by canonical URL
- Keyword-based backend relevance ranking
- Korean daily briefing Markdown generation from Top 3 rankings
- Manual collection endpoint for local verification
- Disabled-by-default daily automation pipeline with daily cost guardrails
- Mini-project quest templates with explicit `TODO-STUDENT` learning boundaries
- Deterministic mini-project skeleton generator
- Portfolio sample artifacts for a daily briefing and one generated mini-project quest
- Technical decision and troubleshooting logs under `docs/`

## Why This Exists

Backend hiring signals change quickly. For a junior backend developer, however, not every trending topic is worth studying. Questack is designed to filter noisy engineering content into a smaller set of topics that are useful for:

- Java/Spring backend interviews
- API, database, messaging, and infrastructure understanding
- AI backend application patterns
- Small projects that can be pushed to GitHub/GitLab


## Architecture

The current flow is:

```text
Manual HTTP Request or Disabled Daily Scheduler
  -> GithubCollectionController
  -> GithubCollector
  -> GithubSearchClient
  -> GitHub Search API
  -> CollectedItem persistence
  -> RssCollectionController
  -> RssCollector
  -> RSS/Atom feeds
  -> CollectedItem persistence
  -> RankingService
  -> RankingScore persistence
  -> DailyBriefingService
  -> Korean Markdown briefing file
  -> MiniProjectQuestTemplate
  -> MiniProjectSkeletonGenerator
  -> generated quest file set with TODO-STUDENT boundaries
```

The first data model is deliberately small:

```text
Source
  |
  | 1:N
  v
CollectedItem
  |
  | 1:1
  v
RankingScore
```

`RankingScore` is separated from `CollectedItem` so ranking rules can evolve without rewriting collected source data.

## Package Structure

```text
src/main/java/com/questack
  collection/
    CollectedItem.java
    CollectedItemRepository.java
    github/
      api/
        GithubCollectionController.java
        dto/
      client/
        GithubSearchClient.java
        dto/
      config/
        GithubProperties.java
      service/
        GithubCollector.java
    rss/
      api/
        RssCollectionController.java
        dto/
      config/
        RssProperties.java
      service/
        RssCollector.java
        RssFeedParser.java
  ranking/
    RankingScore.java
    RankingScoreRepository.java
    api/
    service/
  briefing/
    api/
    config/
    service/
  automation/
    config/
    schedule/
    service/
  quest/
    template/
    generator/
  source/
    Source.java
    SourceRepository.java
```

The GitHub collector is organized by role:

- `api`: receives local HTTP requests
- `api.dto`: response DTOs owned by Questack
- `client`: calls the external GitHub API
- `client.dto`: DTOs shaped by GitHub API responses
- `config`: maps `github.*` configuration
- `service`: owns collection, normalization, duplicate checks, and persistence

## Tech Stack

- Java 21
- Spring Boot 3.5.x
- Spring Web
- Spring Data JPA
- H2 Database
- Spring Actuator
- Spring Validation
- Gradle

## Local Setup

### 1. Export GitHub Token

Questack can call public GitHub Search without a token, but using one makes local development less likely to hit rate limits.

```bash
export GITHUB_TOKEN=your_token_here
```

Do not commit tokens or `.env` files.

### 2. Run Tests

```bash
./gradlew test
```

### 3. Start the Application

```bash
./gradlew bootRun
```

The application starts on port `8080`.

Health check:

```bash
curl -s http://127.0.0.1:8080/actuator/health
```

Expected response:

```json
{"status":"UP"}
```

### 4. Trigger GitHub Collection

```bash
curl -s -X POST "http://127.0.0.1:8080/collections/github?perPage=3"
```

Example response:

```json
{
  "fetchedCount": 3,
  "savedCount": 3,
  "skippedDuplicateCount": 0
}
```

Custom query:

```bash
curl -s -X POST "http://127.0.0.1:8080/collections/github?query=language:Java%20topic:spring-boot%20stars:%3E=100%20fork:false&perPage=10"
```

## MVP Manual Test Walkthrough

Use this flow to test the completed MVP as a user. Keep `./gradlew bootRun` running in one terminal and run the `curl` commands in another terminal.

### 1. Verify the App Is Running

```bash
curl -s http://127.0.0.1:8080/actuator/health
```

Expected response:

```json
{"status":"UP"}
```

### 2. Collect GitHub Signals

```bash
curl -s -X POST "http://127.0.0.1:8080/collections/github?perPage=5"
```

Expected result:

- `fetchedCount` is the number of repositories returned by GitHub.
- `savedCount` is the number of new `CollectedItem` records saved.
- `skippedDuplicateCount` increases when the same canonical URL was already collected.

### 3. Collect RSS/Atom Blog Signals

```bash
curl -s -X POST "http://127.0.0.1:8080/collections/rss"
```

Expected result:

- `feedCount` should be `5` for the MVP source set.
- `savedCount` should increase when new feed items are collected.
- `failedFeedCount` may be greater than `0` if an external feed is temporarily unavailable, but healthy feeds should still be collected.
- `failedFeeds` explains which feed failed and why.

### 4. Rank the Collected Items

```bash
curl -s -X POST "http://127.0.0.1:8080/rankings"
```

Expected result:

- `candidateCount` is the number of collected items without a ranking yet.
- `scoredCount` is the number of items ranked in this run.
- `skippedAlreadyScoredCount` increases if you run ranking again without new collected items.

### 5. Read the Top 3 Backend Topics

```bash
curl -s "http://127.0.0.1:8080/rankings/top?limit=3"
```

Expected result:

- The response is a JSON array with up to 3 ranked items.
- Each item includes title, summary, canonical URL, score fields, total score, and ranking reasons.
- Topics should be backend-career relevant, not frontend-only or hardware-heavy.

### 6. Generate a Daily Briefing

```bash
curl -s -X POST "http://127.0.0.1:8080/briefings/daily?date=2026-06-02"
```

Expected result:

- `itemCount` should be `3` after enough collected items have been ranked.
- `filePath` should point to `docs/daily-briefings/2026-06-02.md`.
- `markdown` should contain a Korean daily briefing with score tables, summaries, interview angles, study paths, and mini project ideas.

Open the generated briefing file:

```bash
sed -n '1,220p' docs/daily-briefings/2026-06-02.md
```

`docs/daily-briefings` is a runtime output directory and is ignored by Git. Committed examples are kept under `docs/samples/daily-briefings`.

### 7. Inspect the Mini Project Quest Sample

The MVP skeleton generator currently produces deterministic file sets in code and the portfolio sample shows what a generated quest looks like:

```bash
find docs/samples/mini-project-quests/spring-ai-rag-document-search -type f | sort
sed -n '1,220p' docs/samples/mini-project-quests/spring-ai-rag-document-search/README.md
```

Expected result:

- The sample includes `README.md`, `acceptance-criteria.md`, `build.gradle`, starter code, and a test skeleton.
- The learning-critical logic is intentionally left as `TODO-STUDENT`.
- The sample demonstrates how one ranked AI/backend topic becomes a junior-backend interview-level hands-on quest.

### 8. Optional: Smoke Test Daily Automation

Daily automation is disabled by default. To smoke test the scheduler locally, stop the running app and start it with automation enabled and a once-per-minute cron:

```bash
AUTOMATION_DAILY_ENABLED=true AUTOMATION_DAILY_CRON="0 * * * * *" ./gradlew bootRun
```

Wait for the next minute boundary, then check whether a briefing for today's date was created under `docs/daily-briefings`.

The default budget allows one GitHub collection run and one RSS collection run per day, and keeps LLM requests at `0` because the MVP does not call an LLM.

## Configuration

`src/main/resources/application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:questack
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: create-drop

github:
  token: ${GITHUB_TOKEN:}
  api-base-url: https://api.github.com
  search-repositories-path: /search/repositories

rss:
  feeds:
    - name: Spring Blog
      url: https://spring.io/blog.atom
      priority: 1
    - name: NAVER D2
      url: https://d2.naver.com/d2.atom
      priority: 2
    - name: Kakao Tech
      url: https://tech.kakao.com/feed/
      priority: 3
    - name: AWS News Blog
      url: https://aws.amazon.com/blogs/aws/feed/
      priority: 4
    - name: InfoQ Software Engineering
      url: https://feed.infoq.com/news/SoftwareDevelopment
      priority: 5

briefing:
  output-directory: docs/daily-briefings

automation:
  daily:
    enabled: false
    cron: "-"
    github-per-page: 10
  cost:
    max-github-collection-runs-per-day: 1
    max-rss-collection-runs-per-day: 1
    max-llm-requests-per-day: 0
```

Notes:

- H2 is used for the MVP to keep local iteration fast.
- `open-in-view` is disabled to keep persistence access inside explicit service boundaries.
- GitHub and RSS collection can still be triggered manually through collection endpoints.
- Daily automation is disabled by default. Set `automation.daily.enabled=true` and provide a cron expression to run collection, ranking, and briefing generation on a schedule.
- Cost guardrails limit daily GitHub collection, RSS collection, and future LLM request usage. The MVP default keeps LLM requests at `0` because Questack does not call an LLM yet.
- The MVP RSS source set includes five configured technical blog feeds.
- Daily briefing Markdown files are written under `docs/daily-briefings` by default.
- Runtime briefing files under `docs/daily-briefings` are ignored by Git unless intentionally copied to `docs/samples`.
- Committed sample output artifacts live under `docs/samples`.

## API

### Collect GitHub Repositories

```http
POST /collections/github
```

Query parameters:

| Name | Default | Description |
| --- | --- | --- |
| `query` | `language:Java topic:spring-boot stars:>=100 fork:false` | GitHub repository search query |
| `perPage` | `10` | Number of repositories to fetch. Clamped to 1-30. |

Response:

```json
{
  "fetchedCount": 10,
  "savedCount": 8,
  "skippedDuplicateCount": 2
}
```

The endpoint intentionally does not use an `/api` prefix. Questack keeps endpoint paths short while the project is still backend-only and internally consumed.

### Collect RSS Feeds

```http
POST /collections/rss
```

Collects configured RSS/Atom feeds and saves new feed items as `CollectedItem` records.

Response:

```json
{
  "feedCount": 5,
  "fetchedCount": 12,
  "savedCount": 10,
  "skippedDuplicateCount": 2,
  "failedFeedCount": 1,
  "failedFeeds": [
    {
      "feedName": "Broken Feed",
      "feedUrl": "https://example.com/broken.xml",
      "reason": "500 Internal Server Error"
    }
  ]
}
```

If one configured feed fails, Questack records it in `failedFeeds` and continues collecting from the remaining feeds.

### Rank Collected Items

```http
POST /rankings
```

Scores collected items that do not have a ranking yet.

Response:

```json
{
  "candidateCount": 10,
  "scoredCount": 10,
  "skippedAlreadyScoredCount": 0
}
```

### Read Top Rankings

```http
GET /rankings/top?limit=3
```

Response:

```json
[
  {
    "rankingScoreId": 1,
    "collectedItemId": 1,
    "title": "example/project",
    "summary": "Spring Boot service example for backend learning",
    "canonicalUrl": "https://github.com/example/project",
    "backendRelevanceScore": 8,
    "learningValueScore": 5,
    "implementationValueScore": 5,
    "totalScore": 18,
    "reasons": "Java/JVM relevance, Spring backend relevance"
  }
]
```

### Generate Daily Briefing

```http
POST /briefings/daily?date=2026-05-27
```

Generates a Korean Markdown briefing from the current Top 3 rankings and writes it to `docs/daily-briefings/{date}.md`.
Each item includes a source-specific summary, importance explanation, practical backend interview question with a follow-up, 30-minute study path, and mini project idea.
Committed sample briefing artifacts live under `docs/samples/daily-briefings`.

Response:

```json
{
  "briefingDate": "2026-05-27",
  "filePath": "docs/daily-briefings/2026-05-27.md",
  "itemCount": 3,
  "markdown": "# 데일리 브리핑: 2026-05-27\n..."
}
```

## Documentation

Project notes are kept in append-only logs:

- `docs/working-guidelines.md`
- `docs/technical-decisions.md`
- `docs/troubleshooting.md`

Planning documents:

- `docs/roadmap/2-week-workflow.md`
- `docs/sources/source-candidates.md`
- `docs/quest/mini-project-quest-template.md`

Generated API snippets:

- `docs/api-docs`

Run `./gradlew test` to regenerate MockMvc Spring REST Docs snippets.

Sample output artifacts:

- `docs/samples`

Portfolio samples:

- `docs/samples/daily-briefings/(sample)-2026-05-29.md`: generated Top 3 briefing sample.
- `docs/samples/mini-project-quests/spring-ai-rag-document-search`: generated mini-project quest skeleton sample.

## Portfolio Demo Story

Questack demonstrates one backend learning loop:

1. Collect signals from GitHub Search and five RSS/Atom technical blog feeds.
2. Normalize external data into `CollectedItem` and prevent duplicates by canonical URL.
3. Rank backend relevance with deterministic keyword rules and labeled quality fixtures.
4. Generate a Korean Top 3 daily briefing with interview angles and 30-minute study paths.
5. Turn one selected topic into a junior-backend mini-project skeleton while leaving the learning-critical code as `TODO-STUDENT`.

Interview self-introduction:

> 매일 변화하는 백엔드/AI 기술을 자동 수집하고, 학습 가능한 미니 프로젝트로 변환하는 서비스를 만들어 보았습니다. GitHub와 기술 블로그 RSS를 같은 도메인 모델로 정규화하고, fixture replay 테스트와 비용 가드로 외부 의존성을 통제했습니다.

Key current decisions:

- Start with GitHub Search and curated technical blogs before social platforms.
- Normalize all external content into `CollectedItem`.
- Collect RSS/Atom technical blog posts through the same `CollectedItem` pipeline.
- Keep ranking in a separate `RankingScore` model.
- Start ranking with deterministic keyword rules before LLM-based summarization.
- Generate daily briefings as Markdown files before introducing a separate briefing table.
- Define mini-project quests with explicit `TODO-STUDENT` learning boundaries before generating project skeletons.
- Use Spring `RestClient` for simple synchronous external HTTP calls.
- Keep GitHub collector code grouped by feature and separated internally by role.
- Keep controller behavior covered by MockMvc REST Docs tests.

## Development Principles

- Check `docs/working-guidelines.md` before making project changes.
- Build the collector pipeline before adding summarization.
- Prefer explicit service boundaries over hidden persistence access.
- Keep external API DTOs separate from internal response DTOs.
- Add or update MockMvc REST Docs tests whenever controller methods are added or changed.
- Record meaningful technical decisions and troubleshooting notes as the project evolves.
- Treat API calls, LLM calls, and crawling as budgeted resources rather than unlimited utilities.

## Commit Guidelines

PRs stay feature-oriented, but commits are split by reviewable intent. See `docs/working-guidelines.md` for the detailed commit boundaries and pre-commit checklist.

## Roadmap

MVP completion:

- GitHub and RSS fixture replay harness: done.
- Top 3 ranking quality fixture: done.
- Five RSS technical blog sources: done.
- Mini-project quest template and skeleton generator: done.
- Reliability behavior for source failures: done.
- Daily automation and cost guardrails: done.
- Portfolio sample briefing and mini-project quest artifacts: done.

Later:

- Evaluate X, Threads, and Instagram only after the GitHub/RSS pipeline is reliable.
- Move from H2 to PostgreSQL for a more production-like setup.
