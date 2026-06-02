# 2-Week Workflow

## Project Summary

Questack is a study automation service for junior backend developer preparation.
It collects backend and AI engineering signals from GitHub and technical blogs, filters them
for Java/Spring/backend relevance, generates a daily briefing, and turns one selected topic
into a small hands-on project.

Name meaning: Quest + Stack.

Self-introduction sentence:

> 매일 변화하는 백엔드/AI 기술을 자동 수집하고, 학습 가능한 미니 프로젝트로 변환하는 서비스를 만들어 보았습니다.

## MVP Constraints

- Daily issue count: 3
- Mini project difficulty: junior backend interview prep
- Primary topics: Java, Spring, backend, databases, infrastructure, AI backend
- Excluded topics: frontend-heavy content, robotics, battery, semiconductor, hardware news
- Initial platform scope: GitHub Search and technical blogs/RSS
- Deferred platform scope: X, Threads, Instagram

## Week 1

### Day 1: Planning and Project Skeleton

- Confirm MVP scope and source priority.
- Create document structure for decisions, troubleshooting, source lists, and daily briefings.
- Define core domain model:
  - Source
  - CollectedItem
  - RankingScore
  - DailyBriefing
  - MiniProjectQuest
- Decide Java/Spring/DB baseline.

Recommended baseline:

- Java 21
- Spring Boot 3.x
- H2 for local MVP
- PostgreSQL as the later production-like option

### Day 2: Spring Boot Setup

- Generate Spring Boot project.
- Add modules/packages:
  - collection
  - ranking
  - briefing
  - quest
  - document
- Add local profiles and environment variable placeholders.
- Add first technical decision record.

### Day 3: GitHub Collector

- Implement GitHub Search API adapter.
- Start with repository search queries:
  - `language:Java topic:spring-boot stars:>=100 pushed:>=YYYY-MM-DD fork:false`
  - `language:Java topic:backend stars:>=50 pushed:>=YYYY-MM-DD fork:false`
  - `spring ai language:Java pushed:>=YYYY-MM-DD`
- Normalize search results into `CollectedItem`.
- Save raw payload fixture for replay testing.

### Day 4: Blog/RSS Collector

- Implement RSS/HTML feed source adapter.
- Add priority 1 technical blogs first.
- Normalize posts into `CollectedItem`.
- Add duplicate detection by canonical URL and normalized title.

### Day 5: Filtering and Ranking

- Implement backend relevance scoring.
- Add positive signals:
  - Java, Spring, Spring Boot, JVM
  - JPA, Hibernate, PostgreSQL, MySQL, Redis
  - Kafka, RabbitMQ, messaging, event-driven
  - OAuth, JWT, security
  - Kubernetes, Docker, observability
  - RAG, LLM, agent, MCP, Spring AI
- Add negative signals:
  - React, Vue, CSS, UI-only
  - robotics, battery, semiconductor, chip manufacturing
  - investment-only or business-only news

### Day 6: Ranking Harness

- Create fixture-based replay tests.
- Label sample collected items as useful/not useful.
- Verify the top 3 items are backend-career relevant.
- Write troubleshooting notes for ranking misses.

### Day 7: Daily Briefing

- Generate daily Markdown briefing:
  - Top 3 issues
  - Why it matters for backend interviews
  - 30-minute study path
  - One implementation challenge
  - Blog draft title and outline
- Store output in `docs/daily-briefings/YYYY-MM-DD.md`.

## Week 2

### Day 8: Mini Project Quest Format

- Define mini project quest template.
- Generate one quest from a selected daily issue.
- Keep core learning code as `TODO-STUDENT`.
- Example quests:
  - Redis cache invalidation in Spring Boot
  - JWT refresh token rotation
  - Kafka consumer retry and dead-letter queue
  - Spring AI RAG document search

### Day 9: Mini Project Generator

- Generate project skeleton from quest metadata.
- Include README, acceptance criteria, tests, and TODO markers.
- Do not fully implement the learning-critical code.

### Day 10: Gamification

- Add simple progress model:
  - streak
  - XP
  - completed quests
  - push checklist
- Award XP for:
  - reading briefing
  - completing TODO-STUDENT code
  - passing tests
  - pushing repository

### Day 11: Automation and Cost Controls

- Add scheduled daily run.
- Add caching for external API responses.
- Add max daily API call count and max LLM request count.
- Use two-stage filtering before expensive summaries.

### Day 12: Reliability Harness

- Add replay harness for saved GitHub/RSS fixtures.
- Add circuit breaker behavior when one source fails.
- Generate troubleshooting draft when collection or ranking fails.

### Day 13: Portfolio Documentation

- Write README.
- Add architecture diagram.
- Polish technical decisions.
- Polish troubleshooting notes.
- Prepare interview explanation.

### Day 14: Demo and Retrospective

- Generate sample daily briefing.
- Generate one mini project quest.
- Push to GitHub/GitLab.
- Finalize self-introduction paragraph.

## Done Criteria

- [x] The service collects from at least GitHub and 5 technical blog sources.
- [x] The service produces exactly 3 backend-relevant daily issues.
- [x] The service creates one junior-backend interview-level mini project quest.
- [x] Technical decisions and troubleshooting notes exist as Markdown docs.
- [x] A replay harness can test collection/ranking without live API calls.
- [x] Portfolio sample artifacts exist under `docs/samples`.

## GitHub Tracking

Current MVP completion milestone:

- [MVP Completion: Replay Harness to Mini Quest](https://github.com/ginsengcandy/Questack/milestone/1)

Use milestone issues as the source of truth for remaining MVP implementation work. The roadmap gives the intended sequence, the milestone defines the active scope, issues define work contracts, and PRs provide reviewed, verified change sets.

Recommended issue order:

1. [x] [#9 Add fixture replay tests for GitHub collection](https://github.com/ginsengcandy/Questack/issues/9)
2. [x] [#8 Add fixture replay tests for RSS collection](https://github.com/ginsengcandy/Questack/issues/8)
3. [x] [#7 Lock Top 3 ranking quality with labeled fixtures](https://github.com/ginsengcandy/Questack/issues/7)
4. [x] [#6 Expand technical blog RSS sources to at least five feeds](https://github.com/ginsengcandy/Questack/issues/6)
5. [x] [#13 Define mini project quest template](https://github.com/ginsengcandy/Questack/issues/13)
6. [x] [#11 Generate mini project skeletons with TODO-STUDENT sections](https://github.com/ginsengcandy/Questack/issues/11)
7. [x] [#10 Add reliability behavior for source failures](https://github.com/ginsengcandy/Questack/issues/10)
8. [x] [#12 Add automation and cost-control guardrails](https://github.com/ginsengcandy/Questack/issues/12)
9. [x] [#14 Prepare portfolio demo and retrospective artifacts](https://github.com/ginsengcandy/Questack/issues/14)

Branch names should include the issue number when practical, for example:

- `test/9-github-fixture-replay`
- `test/8-rss-fixture-replay`
- `feat/13-quest-template`
- `feat/11-quest-skeleton-generator`
- `docs/14-portfolio-demo`
