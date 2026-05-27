# Technical Decisions

This file is append-only. New entries use `TD-###` identifiers and should be referenced from `docs/troubleshooting.md` when relevant.

## TD-001 / 2026-05-24: Questack MVP 범위 채택

**결정 내용:** Questack의 2주 MVP는 GitHub Search와 선별된 기술 블로그/RSS 수집으로 시작한다. X, Threads, Instagram 수집은 랭킹과 일일 브리핑 파이프라인이 안정화된 뒤로 미룬다. 프로젝트 이름은 Quest + Stack을 합친 `Questack`으로 정한다.

**이유 / 배경:** 전체 아이디어는 X, Threads, Instagram, GitHub, 기술 블로그를 모두 포함하지만, 첫 2주에 모든 수집기를 구현하면 API 제약과 노이즈 때문에 핵심 백엔드 역량을 보여주기 어렵다. GitHub와 기술 블로그만으로도 Java, Spring, 백엔드, 인프라, AI 백엔드 관련 신호를 충분히 얻을 수 있고, API 연동, 정규화, 중복 제거, 랭킹, fixture replay 테스트, Markdown 브리핑 생성이라는 백엔드 포트폴리오 요소를 선명하게 보여줄 수 있다.

**대안으로 고려했던 것:** 모든 플랫폼 수집기를 즉시 구현하는 방식 (제품 비전에는 가깝지만 API 마찰과 구현 범위가 큼), GitHub만 먼저 구현하는 방식 (완성은 빠르지만 프로젝트 스토리가 지나치게 좁아짐)

**영향받는 문서 / 파일:** `docs/sources/source-candidates.md`, `docs/roadmap/2-week-workflow.md`, `docs/today-and-tomorrow.md`

## TD-002 / 2026-05-24: 초기 데이터 모델을 수집 출처, 수집 항목, 랭킹 점수로 분리

**결정 내용:** Questack의 초기 데이터 스키마는 `Source`, `CollectedItem`, `RankingScore`를 중심으로 설계한다. `Source`는 GitHub나 기술 블로그 같은 수집 출처를 나타내고, `CollectedItem`은 저장소/블로그 글/아티클 등 실제 수집된 콘텐츠를 나타내며, `RankingScore`는 백엔드 취업 적합도, 학습 가치, 구현 가치 점수를 별도로 저장한다.

**이유 / 배경:** MVP의 핵심 흐름은 수집, 정규화, 중복 제거, 랭킹, 일일 브리핑 생성이다. 수집 원본과 점수 계산 결과를 분리하면 같은 수집 항목에 대해 랭킹 로직을 여러 번 개선하거나 재계산할 수 있고, 이후 GitHub 외 RSS/블로그 수집기를 추가해도 `CollectedItem` 형태로 통일할 수 있다.

**대안으로 고려했던 것:** 수집 항목 하나에 모든 점수 필드를 직접 넣는 방식 (초기 구현은 단순하지만 랭킹 재계산 이력이 흐려짐), GitHub 전용 테이블과 Blog 전용 테이블을 분리하는 방식 (각 소스의 세부 필드는 보존하기 좋지만 브리핑/랭킹 파이프라인이 복잡해짐)

**영향받는 문서 / 파일:** `src/main/java/com/questack/source/Source.java`, `src/main/java/com/questack/collection/CollectedItem.java`, `src/main/java/com/questack/ranking/RankingScore.java`, `src/main/resources/application.yaml`

## TD-003 / 2026-05-24: GitHub Search 수집기를 첫 외부 데이터 연동으로 구현

**결정 내용:** Questack의 첫 외부 수집기는 GitHub Search API 기반 repository collector로 구현한다. `GithubSearchClient`는 GitHub Search API 호출을 담당하고, `GithubCollector`는 응답을 `CollectedItem`으로 정규화해 저장한다. 임시 실행 진입점으로 `POST /collections/github` REST 엔드포인트를 제공한다.

**이유 / 배경:** GitHub는 Java/Spring/backend 트렌드를 가장 빠르게 확인할 수 있는 공개 데이터 소스이며, 신입 백엔드 포트폴리오 관점에서도 API 연동, 설정 분리, 외부 DTO 매핑, 중복 제거, JPA 저장 흐름을 보여주기 좋다. REST 엔드포인트를 먼저 두면 스케줄러를 붙이기 전에 수동으로 수집 과정을 검증할 수 있다.

**대안으로 고려했던 것:** 애플리케이션 시작 시 자동 수집하는 방식 (개발 중 API 호출이 의도치 않게 발생할 수 있음), 스케줄러부터 구현하는 방식 (디버깅 진입점이 부족함), GitHub CLI를 호출하는 방식 (Spring 애플리케이션 내부 기능으로 설명하기 어려움)

**영향받는 문서 / 파일:** `src/main/java/com/questack/collection/github/client/GithubSearchClient.java`, `src/main/java/com/questack/collection/github/service/GithubCollector.java`, `src/main/java/com/questack/collection/github/api/GithubCollectionController.java`, `src/main/resources/application.yaml`

## TD-004 / 2026-05-24: API 서버 기본 설정으로 JPA open-in-view 비활성화

**결정 내용:** Questack의 Spring JPA 설정에서 `spring.jpa.open-in-view`를 `false`로 둔다.

**이유 / 배경:** Questack은 서버 렌더링 화면보다 API와 배치성 수집 파이프라인이 중심인 백엔드 서비스다. open-in-view를 켜두면 웹 요청의 view rendering 단계까지 영속성 컨텍스트가 열려 있어 예상하지 못한 지연 쿼리나 트랜잭션 경계 혼동이 생길 수 있다. 초기부터 service layer 안에서 필요한 데이터를 명시적으로 조회하도록 습관을 잡는 편이 면접 설명에도 좋다.

**대안으로 고려했던 것:** Spring Boot 기본값 유지 (초기 구현은 편하지만 경고가 남고 트랜잭션 경계가 흐려질 수 있음)

**영향받는 문서 / 파일:** `src/main/resources/application.yaml`

## TD-005 / 2026-05-24: 기능별 수집 구현체로 GitHub 패키지 분리

**결정 내용:** `collection.github` 패키지는 GitHub에서 기술 신호를 수집하는 기능 전체를 담당한다. 하위 패키지는 외부 요청을 받는 `api`, GitHub API 호출을 담당하는 `client`, GitHub 응답 DTO를 담는 `client.dto`, 설정을 담는 `config`, 수집 비즈니스 로직을 담는 `service`로 분리한다.

**이유 / 배경:** Questack은 이후 RSS, 블로그, 소셜 플랫폼 수집기를 추가할 계획이 있다. GitHub 관련 controller, 외부 API client, DTO, 설정, service를 한 패키지 안에 섞어두면 다른 수집기가 추가될수록 경계가 흐려진다. 기능 단위 패키지를 유지하되 내부를 역할별로 나누면 GitHub 수집 기능을 독립적으로 이해하고 교체하기 쉽다.

**대안으로 고려했던 것:** `controller`, `service`, `client`를 전역 패키지로 나누는 방식 (초기에는 익숙하지만 수집 소스별 응집도가 약해짐), `collection.github` 바로 아래에 모든 파일을 두는 방식 (파일 수가 적을 때는 단순하지만 확장 시 역할 파악이 어려움)

**영향받는 문서 / 파일:** `src/main/java/com/questack/collection/github/api/GithubCollectionController.java`, `src/main/java/com/questack/collection/github/client/GithubSearchClient.java`, `src/main/java/com/questack/collection/github/config/GithubProperties.java`, `src/main/java/com/questack/collection/github/service/GithubCollector.java`

## TD-006 / 2026-05-24: 외부 HTTP 호출에 Spring RestClient 사용

**결정 내용:** GitHub Search API 호출은 Spring Framework의 `RestClient`를 사용한다.

**이유 / 배경:** Questack의 GitHub 수집은 현재 단순한 동기 HTTP 요청이며, 응답을 받은 뒤 같은 요청 흐름 안에서 `CollectedItem`으로 정규화해 저장한다. `RestClient`는 Spring Boot 3.x 환경에서 별도 의존성 없이 사용할 수 있고, `RestTemplate`보다 현대적인 fluent API를 제공하며, WebFlux 기반의 `WebClient`보다 초기 학습 비용과 설정 부담이 낮다. 신입 백엔드 포트폴리오 관점에서도 동기 HTTP client, header 설정, JSON DTO 매핑 흐름을 설명하기 쉽다.

**대안으로 고려했던 것:** `RestTemplate` (레거시 성격이 강하고 신규 코드에서 선호도가 낮음), `WebClient` (비동기/리액티브 흐름이 필요할 때 강하지만 현재 MVP에는 과함), Java 표준 `HttpClient` (Spring의 message converter와 설정 통합을 직접 챙겨야 함), GitHub CLI 호출 (애플리케이션 내부 HTTP 연동 역량을 보여주기 어려움)

**영향받는 문서 / 파일:** `src/main/java/com/questack/collection/github/client/GithubSearchClient.java`

## TD-007 / 2026-05-26: 초기 랭킹은 결정론적 키워드 규칙으로 구현

**결정 내용:** Questack의 첫 랭킹 로직은 LLM 호출 없이 Java, Spring, JPA, Redis, Kafka, OAuth, JWT, RAG, LLM 같은 긍정 키워드와 frontend, robotics, battery, semiconductor 같은 제외 키워드를 기반으로 점수화한다. 점수는 백엔드 적합도, 학습 가치, 구현 가치로 나누어 `RankingScore`에 저장한다.

**이유 / 배경:** MVP 단계에서는 수집된 항목이 왜 상위에 올랐는지 설명 가능해야 하고, 외부 LLM 호출 비용과 지연 시간을 먼저 늘릴 필요가 없다. 키워드 규칙은 단순하지만 재현 가능하고 테스트하기 쉬우며, 나중에 LLM 요약이나 임베딩 기반 랭킹을 붙이더라도 1차 필터로 계속 사용할 수 있다.

**대안으로 고려했던 것:** LLM으로 모든 수집 항목을 즉시 평가하는 방식 (품질은 기대할 수 있지만 비용과 재현성 문제가 큼), GitHub star 수만으로 정렬하는 방식 (인기도는 볼 수 있지만 백엔드 취업 적합도를 반영하기 어려움), 수집 시점에 바로 점수를 계산하는 방식 (수집과 랭킹의 관심사가 섞이고 규칙 재계산이 불편함)

**영향받는 문서 / 파일:** `src/main/java/com/questack/ranking/service/RankingService.java`, `src/main/java/com/questack/ranking/service/KeywordScoreRule.java`, `src/main/java/com/questack/ranking/api/RankingController.java`, `src/main/java/com/questack/ranking/RankingScore.java`, `README.md`

## TD-008 / 2026-05-26: 컨트롤러 변경은 MockMvc REST Docs 테스트로 고정

**결정 내용:** 앞으로 컨트롤러 메서드를 추가하거나 변경할 때는 MockMvc 기반 컨트롤러 테스트를 함께 작성하거나 수정한다. 테스트는 Spring REST Docs를 사용해 요청 파라미터와 응답 필드를 문서화하고, 생성 스니펫은 `src/docs/api-docs` 아래에 둔다.

**이유 / 배경:** Questack은 수집, 랭킹, 브리핑처럼 외부에서 호출 가능한 API가 점차 늘어날 예정이다. 컨트롤러 테스트를 API 문서 생성과 묶으면 엔드포인트 경로, 파라미터, 응답 형태 변경을 테스트에서 바로 감지할 수 있고, README와 실제 API가 어긋나는 일을 줄일 수 있다. MockMvc를 사용하면 실제 서버를 띄우지 않고도 MVC layer를 빠르게 검증할 수 있다.

**대안으로 고려했던 것:** README에 수동으로 API 예시만 유지하는 방식 (구현과 문서가 쉽게 불일치함), 전체 통합 테스트만 사용하는 방식 (느리고 컨트롤러 계약 변화가 덜 선명함), OpenAPI부터 도입하는 방식 (좋은 선택지지만 현재 MVP에는 설정 범위가 큼)

**영향받는 문서 / 파일:** `build.gradle`, `src/test/java/com/questack/collection/github/api/GithubCollectionControllerTest.java`, `src/test/java/com/questack/ranking/api/RankingControllerTest.java`, `src/docs/api-docs/README.md`
