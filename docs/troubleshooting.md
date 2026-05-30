# Troubleshooting Log

This file is append-only. New entries use `TR-###` identifiers and should cross-reference related technical decisions in `docs/technical-decisions.md`.

## TR-001 / 2026-05-24: Spring Boot 앱이 시작 직후 종료되어 8080 헬스체크 실패

**증상:** `./gradlew bootRun` 실행 결과 `BUILD SUCCESSFUL`이 출력됐지만 8080 포트에서 애플리케이션이 떠 있지 않았고 `/actuator/health` 요청도 실패함. 로그에는 `Started QuestackApplication in 0.774 seconds` 이후 `Closing JPA EntityManagerFactory`, `HikariPool-1 - Shutdown completed`가 바로 출력됨.

**원인:** 가장 가능성이 높은 원인은 `spring-boot-starter-web` 의존성이 빠져 Spring Boot가 웹 애플리케이션이 아니라 일반 애플리케이션으로 시작된 뒤 바로 종료된 것임. 정상적인 웹 애플리케이션이라면 `Tomcat started on port 8080`과 같은 로그가 출력되고 프로세스가 계속 살아 있어야 한다. 또 다른 가능성으로는 `spring.main.web-application-type: none` 설정이 있을 수 있음.

**조사 과정:** H2와 JPA는 정상 초기화됐지만 Tomcat/Netty 웹 서버 초기화 로그가 없음을 확인. Gradle 빌드 성공 여부와 서버 실행 여부는 별개이며, `bootRun`이 즉시 종료되는 경우 웹 서버가 떠 있지 않은 상태로 판단함.

**해결:** `build.gradle`에 `implementation 'org.springframework.boot:spring-boot-starter-web'` 의존성이 있는지 확인하고, 없으면 추가한다. 이후 `./gradlew bootRun`을 다시 실행해 `Tomcat started on port 8080` 로그가 나오는지 확인한다.

**재발 방지:** 초기 Questack 의존성 목록에 `spring-boot-starter-web`을 유지한다. `/actuator/health` smoke test를 실행 체크리스트에 포함한다. Gradle이 `BUILD SUCCESSFUL`을 출력하더라도 `bootRun` 프로세스가 즉시 종료되면 웹 앱 실행 실패로 간주한다.

**관련 항목:** `TD-001`

## TR-002 / 2026-05-24: Gradle test 실행 중 wrapper lock 파일 접근 실패

**증상:** `./gradlew test` 실행 시 `BUILD` 단계에 들어가기 전에 `java.io.FileNotFoundException`이 발생했다. 실패 경로는 `/Users/sehyun/.gradle/wrapper/dists/gradle-8.14.4-bin/.../gradle-8.14.4-bin.zip.lck`였고, 메시지는 `Operation not permitted`였다.

**원인:** 코드나 테스트 실패가 아니라, 현재 실행 환경의 파일 시스템 샌드박스가 프로젝트 외부의 `~/.gradle` wrapper cache lock 파일 접근을 막았다.

**조사 과정:** 오류가 `src`나 JPA 매핑이 아니라 Gradle wrapper 초기화 시점의 `.zip.lck` 파일에서 발생함을 확인했다. 동일한 명령을 Gradle cache 접근 권한이 있는 실행으로 재시도하자 컴파일과 테스트가 모두 통과했다.

**해결:** `./gradlew test`를 Gradle wrapper cache 접근 권한이 있는 환경에서 다시 실행했다. 재실행 결과 `BUILD SUCCESSFUL`로 통과했다.

**재발 방지:** Gradle wrapper, dependency cache, test 실행이 `~/.gradle`에 접근해야 할 수 있음을 인지한다. 같은 `Operation not permitted` 오류가 Gradle cache 경로에서 발생하면 코드 문제로 보기 전에 실행 권한 문제를 먼저 확인한다.

**관련 항목:** 없음

## TR-003 / 2026-05-24: 샌드박스 내부 curl에서 로컬 8080 헬스체크 연결 실패

**증상:** `bootRun` 로그에는 `Tomcat started on port 8080`이 출력됐지만, 일반 실행 환경에서 `curl http://127.0.0.1:8080/actuator/health` 요청이 exit code 7로 실패했다.

**원인:** Questack 서버는 권한 승인된 환경에서 실행 중이었고, 일반 샌드박스 실행 환경의 로컬 네트워크 접근이 같은 방식으로 연결되지 않았다. 애플리케이션 자체의 기동 실패가 아니라 실행 환경 경계 문제였다.

**조사 과정:** `bootRun` 로그에서 Tomcat 8080 기동과 애플리케이션 시작 완료를 확인했다. 동일한 health check를 권한 승인된 curl 실행으로 재시도하자 `{"status":"UP"}` 응답을 받았다.

**해결:** 로컬 서버 검증용 curl을 권한 승인된 환경에서 실행했다.

**재발 방지:** 서버 프로세스를 권한 승인된 실행으로 띄운 경우, 로컬 검증 요청도 같은 접근 권한에서 수행한다. `curl` 실패만으로 앱 기동 실패로 판단하지 말고, 서버 로그와 health check 실행 환경을 함께 확인한다.

**관련 항목:** 없음

## TR-004 / 2026-05-27: 수집과 랭킹을 병렬 호출해 랭킹 대상이 0건으로 처리됨

**증상:** 데일리 브리핑 수동 검증 중 `POST /collections/github`와 `POST /rankings`를 병렬로 호출했더니 GitHub 수집은 3건 저장됐지만 랭킹 응답은 `candidateCount: 0`, `scoredCount: 0`으로 반환됨.

**원인:** 랭킹은 이미 저장된 `CollectedItem`을 조회해 점수화하는 후속 단계인데, 수집 트랜잭션이 커밋되기 전에 랭킹 요청이 먼저 실행됐다. 애플리케이션 로직 오류라기보다 검증 명령을 순차 의존 흐름과 다르게 실행한 문제였다.

**조사 과정:** 수집 응답은 `fetchedCount: 3`, `savedCount: 3`으로 정상 반환됐다. 이후 `POST /rankings`를 다시 순차 실행하자 `candidateCount: 3`, `scoredCount: 3`으로 정상 처리됐고, `GET /rankings/top?limit=3`에서도 Top 3 결과를 확인했다.

**해결:** 수동 검증 순서를 `수집 -> 랭킹 -> 브리핑 생성`으로 순차 실행했다.

**재발 방지:** 검증 명령은 의존성이 있는 작업끼리 병렬 실행하지 않는다. 추후 하나의 오케스트레이션 엔드포인트나 스케줄러를 만들 때도 수집 완료 후 랭킹, 랭킹 완료 후 브리핑 생성 순서를 명시한다.

**관련 항목:** `TD-009`

## TR-005 / 2026-05-29: RSS 수집 중 summary 컬럼 길이 초과로 500 응답

**증상:** `POST /collections/rss` 수동 검증 중 500 응답이 발생했다. 서버 로그에는 H2 `Value too long for column "SUMMARY CHARACTER VARYING(2000)"` 오류가 출력됐고, NAVER D2 feed의 description 값이 2000자를 초과했다.

**원인:** RSS description은 짧은 요약이 아니라 HTML 본문 일부를 포함할 수 있다. 기존 `CollectedItem.summary` 컬럼은 2000자로 제한되어 있는데, RSS 수집기가 feed description을 정규화하거나 자르지 않고 그대로 저장했다.

**조사 과정:** 서버 로그에서 `collected_items.summary` insert 시점에 길이 초과가 발생한 것을 확인했다. GitHub 수집과 달리 RSS/Atom feed는 description/content에 이미지 태그와 긴 HTML 조각이 포함될 수 있음을 확인했다.

**해결:** RSS 수집 저장 전 `RssTextNormalizer`를 통해 HTML 태그를 제거하고, title/summary/externalId/author를 `CollectedItem` 컬럼 길이에 맞게 제한했다. summary는 2000자, title은 300자, externalId와 author는 100자로 제한한다.

**재발 방지:** 외부 source adapter는 `CollectedItem`으로 정규화하기 전에 컬럼 길이와 텍스트 품질을 맞춘다. 새 collector를 추가할 때도 원본 payload를 그대로 저장하지 않고 정규화/절단 정책을 먼저 둔다.

**관련 항목:** `TD-013`

## TR-006 / 2026-05-30: fixture replay 테스트 추가 후 RankingServiceTest source unique 제약 충돌

**증상:** GitHub fixture replay 테스트를 추가한 뒤 `./gradlew test` 실행 중 `RankingServiceTest`가 `DataIntegrityViolationException`으로 실패했다. 실패 지점은 테스트용 `Source("Test GitHub", ...)` 저장 시점이었다.

**원인:** 여러 `@SpringBootTest`가 같은 H2 application context를 재사용할 수 있는데, 기존 `RankingServiceTest`는 테스트 시작 전에 repository 데이터를 정리하지 않았다. 하네스 테스트가 늘어나면서 이전 테스트 데이터가 남아 `sources.name` unique 제약과 충돌했다.

**조사 과정:** 새 GitHub replay 테스트는 `GitHub Search` source를 사용했지만, 실패는 기존 ranking 테스트의 `Test GitHub` source 저장에서 발생했다. 코드 경로보다 테스트 간 DB 상태 공유 문제로 판단했고, ranking score, collected item, source 순서로 데이터를 정리하도록 보강했다.

**해결:** `GithubCollectorReplayTest`와 `RankingServiceTest`에 `@BeforeEach` cleanup을 추가했다. FK 제약을 고려해 `RankingScoreRepository`, `CollectedItemRepository`, `SourceRepository` 순서로 `deleteAll()`을 호출한다. 이후 `./gradlew test`가 통과했다.

**재발 방지:** repository를 직접 사용하는 Spring Boot 테스트는 테스트 시작 전에 필요한 저장소를 명시적으로 정리한다. 특히 `Source.name`과 `CollectedItem.canonicalUrl`처럼 unique 제약이 있는 테이블은 테스트 순서나 context cache에 의존하지 않는다.

**관련 항목:** `TD-017`
