# Questack

영문 원본: [README.md](README.md). `README.md`가 단일 진실의 원천이며, 이 한국어 버전은 번역본입니다. 영문 README가 변경될 때마다 한국어 버전에도 변경 사항을 반영해야 합니다.

Questack은 백엔드 중심의 학습 자동화 서비스입니다.

이름은 **Quest + Stack**을 뜻합니다. 제품 아이디어는 단순합니다. 매일 백엔드와 AI 엔지니어링 신호를 수집하고, Java/Spring/backend 관련성을 기준으로 필터링한 뒤, 유용한 항목을 짧은 학습 퀘스트와 미니 프로젝트로 바꿉니다.

이 저장소는 기능이 과하게 많은 프로토타입보다 작고 살펴보기 쉬운 Spring Boot 백엔드로 의도적으로 구성되어 있습니다. MVP는 외부 source 연동, 정규화된 저장, 중복 처리, 랭킹, 데일리 브리핑 출력, 미니 프로젝트 퀘스트 생성으로 이어지는 안정적인 학습 파이프라인에 집중합니다.

## 현재 범위

구현됨:

- Java 21 기반 Spring Boot 3.5.x 애플리케이션
- H2 기반 로컬 persistence
- GitHub Search API 저장소 수집
- RSS/Atom 기술 블로그 수집
- 정규화된 source와 collected item 모델
- canonical URL 기반 중복 방지
- 키워드 기반 백엔드 관련성 랭킹
- Top 3 랭킹 기반 한국어 데일리 브리핑 Markdown 생성
- 로컬 검증을 위한 수동 수집 endpoint
- 기본 비활성화된 daily automation pipeline과 daily cost guardrail
- 명시적인 `TODO-STUDENT` 학습 경계를 가진 mini-project quest template
- 결정론적 mini-project skeleton generator
- 데일리 브리핑과 생성된 mini-project quest 샘플 artifact
- `docs/` 아래 technical decision과 troubleshooting log

## 존재 이유

백엔드 채용 신호는 빠르게 바뀝니다. 하지만 주니어 백엔드 개발자에게 모든 트렌딩 주제가 공부할 가치가 있는 것은 아닙니다. Questack은 시끄러운 엔지니어링 콘텐츠를 다음 목적에 유용한 작은 topic set으로 필터링하기 위해 만들어졌습니다.

- Java/Spring 백엔드 면접
- API, database, messaging, infrastructure 이해
- AI backend application pattern
- GitHub/GitLab에 push할 수 있는 작은 프로젝트

## 아키텍처

현재 흐름은 다음과 같습니다.

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

첫 데이터 모델은 의도적으로 작게 유지합니다.

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

`RankingScore`는 `CollectedItem`과 분리되어 있으므로, 수집된 source data를 다시 쓰지 않고도 ranking rule을 개선하거나 재계산할 수 있습니다.

## 패키지 구조

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

GitHub collector는 역할별로 구성되어 있습니다.

- `api`: 로컬 HTTP 요청 수신
- `api.dto`: Questack이 소유하는 response DTO
- `client`: 외부 GitHub API 호출
- `client.dto`: GitHub API 응답 형태의 DTO
- `config`: `github.*` 설정 매핑
- `service`: 수집, 정규화, 중복 검사, 저장 흐름 담당

## 기술 스택

- Java 21
- Spring Boot 3.5.x
- Spring Web
- Spring Data JPA
- H2 Database
- Spring Actuator
- Spring Validation
- Gradle

## 로컬 설정

### 1. GitHub Token Export

Questack은 token 없이도 public GitHub Search를 호출할 수 있지만, token을 사용하면 로컬 개발 중 rate limit에 걸릴 가능성이 줄어듭니다.

```bash
export GITHUB_TOKEN=your_token_here
```

token이나 `.env` 파일은 커밋하지 마세요.

### 2. 테스트 실행

```bash
./gradlew test
```

### 3. 애플리케이션 시작

```bash
./gradlew bootRun
```

애플리케이션은 `8080` port에서 시작합니다.

Health check:

```bash
curl -s http://127.0.0.1:8080/actuator/health
```

예상 응답:

```json
{"status":"UP"}
```

### 4. GitHub 수집 실행

```bash
curl -s -X POST "http://127.0.0.1:8080/collections/github?perPage=3"
```

예시 응답:

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

## MVP 수동 테스트 Walkthrough

완성된 MVP를 사용자 입장에서 테스트하려면 이 흐름을 사용하세요. 한 terminal에서는 `./gradlew bootRun`을 계속 실행하고, 다른 terminal에서 `curl` 명령을 실행합니다.

### 1. 앱 실행 확인

```bash
curl -s http://127.0.0.1:8080/actuator/health
```

예상 응답:

```json
{"status":"UP"}
```

### 2. GitHub 신호 수집

```bash
curl -s -X POST "http://127.0.0.1:8080/collections/github?perPage=5"
```

예상 결과:

- `fetchedCount`는 GitHub가 반환한 repository 수입니다.
- `savedCount`는 새로 저장된 `CollectedItem` record 수입니다.
- 같은 canonical URL이 이미 수집되어 있으면 `skippedDuplicateCount`가 증가합니다.

### 3. RSS/Atom 블로그 신호 수집

```bash
curl -s -X POST "http://127.0.0.1:8080/collections/rss"
```

예상 결과:

- MVP source set 기준 `feedCount`는 `5`여야 합니다.
- 새 feed item이 수집되면 `savedCount`가 증가합니다.
- 외부 feed가 일시적으로 unavailable이면 `failedFeedCount`가 `0`보다 클 수 있지만, 정상 feed는 계속 수집되어야 합니다.
- `failedFeeds`는 어떤 feed가 왜 실패했는지 설명합니다.

### 4. 수집 항목 랭킹

```bash
curl -s -X POST "http://127.0.0.1:8080/rankings"
```

예상 결과:

- `candidateCount`는 아직 ranking이 없는 collected item 수입니다.
- `scoredCount`는 이번 실행에서 ranking된 item 수입니다.
- 새 collected item 없이 ranking을 다시 실행하면 `skippedAlreadyScoredCount`가 증가합니다.

### 5. Top 3 백엔드 topic 조회

```bash
curl -s "http://127.0.0.1:8080/rankings/top?limit=3"
```

예상 결과:

- 응답은 최대 3개의 ranked item을 담은 JSON array입니다.
- 각 item은 title, summary, canonical URL, score field, total score, ranking reason을 포함합니다.
- topic은 frontend-only나 hardware-heavy가 아니라 backend career와 관련 있어야 합니다.

### 6. 데일리 브리핑 생성

```bash
curl -s -X POST "http://127.0.0.1:8080/briefings/daily?date=2026-06-02"
```

예상 결과:

- 충분한 collected item이 ranking된 뒤라면 `itemCount`는 `3`이어야 합니다.
- `filePath`는 `docs/daily-briefings/2026-06-02.md`를 가리켜야 합니다.
- `markdown`은 score table, summary, interview angle, study path, mini project idea를 포함한 한국어 데일리 브리핑이어야 합니다.

생성된 브리핑 파일 열기:

```bash
sed -n '1,220p' docs/daily-briefings/2026-06-02.md
```

`docs/daily-briefings`는 runtime output directory이며 Git에서 ignore됩니다. 커밋된 예시는 `docs/samples/daily-briefings` 아래에 보관합니다.

### 7. Mini Project Quest 샘플 확인

MVP skeleton generator는 현재 코드에서 deterministic file set을 생성하며, portfolio sample은 생성된 quest가 어떤 모습인지 보여줍니다.

```bash
find docs/samples/mini-project-quests/spring-ai-rag-document-search -type f | sort
sed -n '1,220p' docs/samples/mini-project-quests/spring-ai-rag-document-search/README.md
```

예상 결과:

- 샘플은 `README.md`, `acceptance-criteria.md`, `build.gradle`, starter code, test skeleton을 포함합니다.
- 학습 핵심 로직은 의도적으로 `TODO-STUDENT`로 남겨둡니다.
- 샘플은 ranking된 AI/backend topic 하나가 junior-backend interview 수준의 hands-on quest가 되는 방식을 보여줍니다.

### 8. 선택 사항: Daily Automation Smoke Test

Daily automation은 기본적으로 비활성화되어 있습니다. scheduler를 로컬에서 smoke test하려면 실행 중인 앱을 멈추고 automation을 켠 뒤 1분마다 실행되는 cron으로 시작합니다.

```bash
AUTOMATION_DAILY_ENABLED=true AUTOMATION_DAILY_CRON="0 * * * * *" ./gradlew bootRun
```

다음 minute boundary까지 기다린 뒤, 오늘 날짜의 briefing이 `docs/daily-briefings` 아래에 생성되었는지 확인합니다.

기본 budget은 하루에 GitHub collection 1회와 RSS collection 1회를 허용하며, MVP는 LLM을 호출하지 않으므로 LLM request는 `0`으로 유지합니다.

## 설정

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

- H2는 MVP에서 빠른 local iteration을 위해 사용합니다.
- `open-in-view`는 persistence access를 명시적인 service boundary 안에 두기 위해 비활성화합니다.
- GitHub와 RSS collection은 collection endpoint를 통해 수동으로 실행할 수 있습니다.
- Daily automation은 기본적으로 비활성화되어 있습니다. collection, ranking, briefing generation을 schedule로 실행하려면 `automation.daily.enabled=true`로 설정하고 cron expression을 제공합니다.
- Cost guardrail은 daily GitHub collection, RSS collection, future LLM request 사용량을 제한합니다. Questack MVP는 아직 LLM을 호출하지 않으므로 기본 LLM request는 `0`입니다.
- MVP RSS source set에는 5개의 configured technical blog feed가 포함됩니다.
- Daily briefing Markdown file은 기본적으로 `docs/daily-briefings` 아래에 작성됩니다.
- `docs/daily-briefings` 아래 runtime briefing file은 의도적으로 `docs/samples`에 복사하지 않는 한 Git에서 ignore됩니다.
- 커밋된 sample output artifact는 `docs/samples` 아래에 보관합니다.

## API

### GitHub Repository 수집

```http
POST /collections/github
```

Query parameter:

| Name | Default | Description |
| --- | --- | --- |
| `query` | `language:Java topic:spring-boot stars:>=100 fork:false` | GitHub repository search query |
| `perPage` | `10` | 가져올 repository 수. 1-30 사이로 clamp됩니다. |

Response:

```json
{
  "fetchedCount": 10,
  "savedCount": 8,
  "skippedDuplicateCount": 2
}
```

이 endpoint는 의도적으로 `/api` prefix를 사용하지 않습니다. Questack은 아직 backend-only이며 내부적으로 소비되는 프로젝트이므로 endpoint path를 짧게 유지합니다.

### RSS Feed 수집

```http
POST /collections/rss
```

설정된 RSS/Atom feed를 수집하고 새 feed item을 `CollectedItem` record로 저장합니다.

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

설정된 feed 하나가 실패하면 Questack은 이를 `failedFeeds`에 기록하고 나머지 feed 수집을 계속합니다.

### Collected Item Ranking

```http
POST /rankings
```

아직 ranking이 없는 collected item에 점수를 부여합니다.

Response:

```json
{
  "candidateCount": 10,
  "scoredCount": 10,
  "skippedAlreadyScoredCount": 0
}
```

### Top Ranking 조회

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

### Daily Briefing 생성

```http
POST /briefings/daily?date=2026-05-27
```

현재 Top 3 ranking을 기반으로 한국어 Markdown briefing을 생성하고 `docs/daily-briefings/{date}.md`에 씁니다.
각 item은 source-specific summary, importance explanation, follow-up이 포함된 practical backend interview question, 30-minute study path, mini project idea를 포함합니다.
커밋된 sample briefing artifact는 `docs/samples/daily-briefings` 아래에 있습니다.

Response:

```json
{
  "briefingDate": "2026-05-27",
  "filePath": "docs/daily-briefings/2026-05-27.md",
  "itemCount": 3,
  "markdown": "# 데일리 브리핑: 2026-05-27\n..."
}
```

## 문서

프로젝트 note는 append-only log로 관리합니다.

- `docs/working-guidelines.md`
- `docs/technical-decisions.md`
- `docs/troubleshooting.md`

계획 문서:

- `docs/roadmap/2-week-workflow.md`
- `docs/sources/source-candidates.md`
- `docs/quest/mini-project-quest-template.md`

생성된 API snippet:

- `docs/api-docs`

MockMvc Spring REST Docs snippet을 다시 생성하려면 `./gradlew test`를 실행합니다.

Sample output artifact:

- `docs/samples`

Portfolio sample:

- `docs/samples/daily-briefings/(sample)-2026-05-29.md`: 생성된 Top 3 briefing sample.
- `docs/samples/mini-project-quests/spring-ai-rag-document-search`: 생성된 mini-project quest skeleton sample.

## Portfolio Demo Story

Questack은 하나의 backend learning loop를 보여줍니다.

1. GitHub Search와 5개의 RSS/Atom technical blog feed에서 신호를 수집합니다.
2. 외부 데이터를 `CollectedItem`으로 정규화하고 canonical URL로 중복을 방지합니다.
3. 결정론적 keyword rule과 labeled quality fixture로 backend relevance를 ranking합니다.
4. interview angle과 30-minute study path가 포함된 한국어 Top 3 daily briefing을 생성합니다.
5. 선택된 topic 하나를 junior-backend mini-project skeleton으로 바꾸되, learning-critical code는 `TODO-STUDENT`로 남깁니다.

Interview self-introduction:

> 매일 변화하는 백엔드/AI 기술을 자동 수집하고, 학습 가능한 미니 프로젝트로 변환하는 서비스를 만들어 보았습니다. GitHub와 기술 블로그 RSS를 같은 도메인 모델로 정규화하고, fixture replay 테스트와 비용 가드로 외부 의존성을 통제했습니다.

현재 주요 결정:

- social platform보다 GitHub Search와 curated technical blog로 시작합니다.
- 모든 외부 content를 `CollectedItem`으로 정규화합니다.
- RSS/Atom technical blog post도 같은 `CollectedItem` pipeline으로 수집합니다.
- ranking은 별도 `RankingScore` model로 유지합니다.
- LLM 기반 summarization 전에 deterministic keyword rule로 ranking을 시작합니다.
- 별도 briefing table을 도입하기 전에 daily briefing을 Markdown file로 생성합니다.
- project skeleton을 생성하기 전에 명시적인 `TODO-STUDENT` learning boundary를 가진 mini-project quest를 정의합니다.
- 단순 synchronous external HTTP call에는 Spring `RestClient`를 사용합니다.
- GitHub collector code는 feature별로 묶고 내부에서 role별로 분리합니다.
- controller behavior는 MockMvc REST Docs test로 보호합니다.

## 개발 원칙

- 프로젝트 변경 전 `docs/working-guidelines.md`를 확인합니다.
- summarization을 추가하기 전에 collector pipeline을 먼저 구축합니다.
- 숨겨진 persistence access보다 명시적인 service boundary를 선호합니다.
- external API DTO와 internal response DTO를 분리합니다.
- controller method를 추가하거나 변경할 때는 MockMvc REST Docs test를 추가하거나 수정합니다.
- 의미 있는 technical decision과 troubleshooting note를 프로젝트 변화에 맞춰 기록합니다.
- API call, LLM call, crawling을 무제한 utility가 아니라 budgeted resource로 취급합니다.

## Commit Guidelines

PR은 feature-oriented로 유지하되, commit은 reviewable intent 기준으로 나눕니다. 자세한 commit boundary와 pre-commit checklist는 `docs/working-guidelines.md`를 참고하세요.

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

- GitHub/RSS pipeline이 안정화된 뒤 X, Threads, Instagram을 평가합니다.
- 더 production-like한 setup을 위해 H2에서 PostgreSQL로 이동합니다.
