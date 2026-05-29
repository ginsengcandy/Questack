package com.questack.briefing.service;

import com.questack.briefing.api.dto.DailyBriefingResponse;
import com.questack.briefing.config.BriefingProperties;
import com.questack.ranking.api.dto.RankingScoreResponse;
import com.questack.ranking.service.RankingService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DailyBriefingService {

    private static final int DAILY_BRIEFING_LIMIT = 3;

    private final RankingService rankingService;
    private final BriefingProperties briefingProperties;

    public DailyBriefingService(RankingService rankingService, BriefingProperties briefingProperties) {
        this.rankingService = rankingService;
        this.briefingProperties = briefingProperties;
    }

    public DailyBriefingResponse generate(LocalDate briefingDate) {
        List<RankingScoreResponse> topRankings = rankingService.findTopRankings(DAILY_BRIEFING_LIMIT);
        String markdown = renderMarkdown(briefingDate, topRankings);
        Path outputPath = writeMarkdown(briefingDate, markdown);

        return new DailyBriefingResponse(
                briefingDate,
                outputPath.toString(),
                topRankings.size(),
                markdown
        );
    }

    private String renderMarkdown(LocalDate briefingDate, List<RankingScoreResponse> rankings) {
        StringBuilder builder = new StringBuilder();
        builder.append("# 데일리 브리핑: ").append(briefingDate).append(System.lineSeparator());
        builder.append(System.lineSeparator());

        for (int index = 0; index < rankings.size(); index++) {
            RankingScoreResponse ranking = rankings.get(index);
            builder.append("## ").append(index + 1).append(". ").append(ranking.title()).append(System.lineSeparator());
            builder.append(System.lineSeparator());
            builder.append("- Source: ").append(ranking.canonicalUrl()).append(System.lineSeparator());
            builder.append(System.lineSeparator());
            builder.append(scoreTable(ranking));
            builder.append("- Summary: ").append(summary(ranking)).append(System.lineSeparator());
            builder.append("- Why it matters: ").append(whyItMatters(ranking)).append(System.lineSeparator());
            builder.append("- Backend interview angle: ").append(backendInterviewAngle(ranking)).append(System.lineSeparator());
            builder.append("- 30-minute study path: ").append(studyPath(ranking)).append(System.lineSeparator());
            builder.append("- Mini project idea: ").append(miniProjectIdea(ranking)).append(System.lineSeparator());
            builder.append(System.lineSeparator());
        }

        if (rankings.isEmpty()) {
            builder.append("아직 브리핑을 생성할 랭킹 데이터가 없습니다. 먼저 GitHub 수집과 랭킹 생성을 실행해 주세요.")
                    .append(System.lineSeparator());
        }

        return builder.toString();
    }

    private String scoreTable(RankingScoreResponse ranking) {
        return """
                | 평가 항목 | 점수 |
                | --- | ---: |
                | 백엔드 적합도 | %d |
                | 학습 가치 | %d |
                | 구현 가치 | %d |
                | 총점 | %d |

                """.formatted(
                ranking.backendRelevanceScore(),
                ranking.learningValueScore(),
                ranking.implementationValueScore(),
                ranking.totalScore()
        );
    }

    private String summary(RankingScoreResponse ranking) {
        TopicBriefingProfile profile = profileFor(ranking);
        String sourceSummary = conciseSummary(ranking.summary());
        if (sourceSummary.isBlank()) {
            return "%s 소재입니다. %s".formatted(profile.topic(), profile.summaryFocus());
        }
        return "%s %s".formatted(sourceSummary, profile.summaryFocus());
    }

    private String whyItMatters(RankingScoreResponse ranking) {
        TopicBriefingProfile profile = profileFor(ranking);
        return "%s. 랭킹 근거는 %s입니다. %s"
                .formatted(profile.whyItMatters(), ranking.reasons(), profile.backendValue());
    }

    private String backendInterviewAngle(RankingScoreResponse ranking) {
        return profileFor(ranking).interviewQuestion();
    }

    private String studyPath(RankingScoreResponse ranking) {
        TopicBriefingProfile profile = profileFor(ranking);
        return "10분: 원문에서 %s만 표시하며 읽습니다. 10분: `%s`를 짧게 리서치합니다. 10분: Spring Boot에서 검증할 수 있는 체크리스트 3개를 적습니다. 참고 URL: %s"
                .formatted(profile.readingFocus(), profile.researchTopic(), ranking.canonicalUrl());
    }

    private String miniProjectIdea(RankingScoreResponse ranking) {
        return profileFor(ranking).miniProjectIdea();
    }

    private TopicBriefingProfile profileFor(RankingScoreResponse ranking) {
        String text = String.join(" ",
                nullToEmpty(ranking.title()),
                nullToEmpty(ranking.summary()),
                nullToEmpty(ranking.reasons()),
                nullToEmpty(ranking.canonicalUrl())
        ).toLowerCase();

        if (containsAny(text, "oracle", "mysql", "migration", "마이그레이션", "무중단", "전환")) {
            return new TopicBriefingProfile(
                    "데이터베이스 무중단 전환",
                    "핵심은 기존 DB와 신규 DB를 동시에 다루는 동안 데이터 정합성, 전환 순서, rollback 지점을 어떻게 관리했는지입니다.",
                    "운영 중인 서비스의 DB를 바꾸는 일은 단순 스키마 변경이 아니라 쓰기 경로, 읽기 검증, 배포 순서, 장애 대응이 모두 얽힌 백엔드 실무 과제입니다",
                    "지원자가 트랜잭션과 데이터 정합성을 실제 운영 조건에서 이해하는지 확인하기 좋습니다.",
                    "Oracle에서 MySQL로 무중단 이전해야 한다면 dual-write, CDC, bulk migration, read verification 중 어떤 순서로 설계하겠습니까? 꼬리 질문: 전환 직후 일부 데이터 불일치가 발견되면 어떤 지표와 로그를 먼저 확인하고 rollback 여부를 어떻게 판단하겠습니까?",
                    "데이터 흐름, 검증 지점, rollback 조건",
                    "zero-downtime database migration rollback strategy",
                    "Spring Boot 주문 API를 H2의 old/new 테이블 두 벌로 구성하고, 쓰기는 양쪽에 반영하되 읽기는 feature flag로 전환하는 미니 마이그레이션을 구현합니다. TODO-STUDENT는 데이터 검증 쿼리와 전환 조건 판단 로직으로 둡니다."
            );
        }

        if (containsAny(text, "spring-boot-admin", "actuator", "observability", "monitoring", "health", "metrics")) {
            return new TopicBriefingProfile(
                    "Spring Boot 운영 모니터링",
                    "핵심은 여러 Spring Boot 애플리케이션의 health, metrics, environment, log level 같은 운영 신호를 한곳에서 확인하는 방식입니다.",
                    "서비스가 배포된 뒤에는 API 기능만큼이나 상태 확인, 장애 탐지, 운영 액션을 안전하게 노출하는 설계가 중요합니다",
                    "Actuator endpoint를 어떤 범위로 공개하고 어떤 인증/네트워크 경계로 보호할지 설명할 수 있어야 합니다.",
                    "Spring Boot Actuator를 운영에 열 때 health, metrics, env endpoint의 공개 범위를 어떻게 나누겠습니까? 꼬리 질문: admin 서버가 장애 나거나 endpoint가 과도하게 노출될 때 생길 수 있는 보안/운영 리스크는 무엇입니까?",
                    "어떤 endpoint가 운영 판단에 쓰이는지, 어떤 endpoint는 숨겨야 하는지",
                    "Spring Boot Actuator health groups and endpoint exposure",
                    "두 개의 Spring Boot 앱과 하나의 admin 역할 앱을 만들고, `/actuator/health` 결과를 수집해 간단한 상태 대시보드 API로 노출합니다. TODO-STUDENT는 DOWN 상태 감지와 알림 이벤트 생성 로직으로 둡니다."
            );
        }

        if (containsAny(text, "spring ai", "rag", "llm", "vector", "embedding", "agent", "mcp")) {
            return new TopicBriefingProfile(
                    "Spring AI 기반 AI 백엔드",
                    "핵심은 Spring 생태계에서 LLM, RAG, tool calling, vector store 같은 AI 기능을 백엔드 애플리케이션 구조 안에 넣는 방법입니다.",
                    "AI 기능은 단순 API 호출보다 프롬프트 입력 검증, 검색 컨텍스트 품질, 비용 제한, 실패 fallback을 함께 설계해야 실서비스 기능이 됩니다",
                    "지원자가 AI 기능을 컨트롤러에 바로 붙이는 수준을 넘어 서비스 경계, 테스트, 비용/장애 제어까지 생각하는지 보기 좋습니다.",
                    "Spring AI로 RAG 검색 API를 만든다면 문서 적재, embedding, retrieval, generation을 어떤 계층으로 나누겠습니까? 꼬리 질문: 답변 품질이 낮거나 LLM 호출이 실패할 때 사용자 응답과 로그를 어떻게 설계하겠습니까?",
                    "릴리스 노트에서 바뀐 API, 의존성 버전, migration note",
                    "Spring AI RAG evaluation and fallback strategy",
                    "문서 3개를 메모리 저장소에 넣고 질문을 받으면 관련 문서 조각을 찾아 답변 초안을 만드는 RAG API를 구현합니다. TODO-STUDENT는 검색 결과를 프롬프트 컨텍스트로 조립하는 로직과 fallback 응답 정책으로 둡니다."
            );
        }

        if (containsAny(text, "redis", "cache", "caching")) {
            return new TopicBriefingProfile(
                    "캐시 전략",
                    "핵심은 반복 조회 비용을 줄이면서도 stale data, invalidation, TTL을 어떻게 통제할지입니다.",
                    "캐시는 성능 개선 도구이지만 데이터 일관성, 장애 시 fallback, hot key 문제를 함께 다뤄야 하는 백엔드 설계 주제입니다",
                    "지원자가 캐시를 붙이는 것과 운영 가능한 캐시 전략을 구분하는지 확인할 수 있습니다.",
                    "상품 상세 API에 Redis cache-aside를 적용한다면 TTL과 invalidation은 어떻게 정하겠습니까? 꼬리 질문: DB 업데이트 성공 후 캐시 삭제가 실패하면 어떤 문제가 생기고 어떻게 보완하겠습니까?",
                    "캐시 hit/miss 경로와 stale data가 생기는 조건",
                    "cache-aside invalidation race condition",
                    "상품 조회 API에 cache-aside를 붙이고 수정 API에서 캐시를 무효화합니다. TODO-STUDENT는 동시 수정 상황에서 stale cache를 줄이는 테스트와 보완 로직으로 둡니다."
            );
        }

        if (containsAny(text, "kafka", "rabbitmq", "message", "messaging", "event-driven", "dlq")) {
            return new TopicBriefingProfile(
                    "메시징과 이벤트 처리",
                    "핵심은 요청 처리와 후속 작업을 분리하면서 재시도, 중복 처리, 실패 격리를 어떻게 설계할지입니다.",
                    "메시징은 트래픽 흡수와 시스템 분리에 유용하지만 at-least-once 처리, idempotency, DLQ 없이는 장애가 데이터 문제로 번질 수 있습니다",
                    "지원자가 이벤트 기반 구조의 장점뿐 아니라 운영 실패 모드까지 이해하는지 확인하기 좋습니다.",
                    "주문 생성 후 알림/정산 이벤트를 Kafka로 처리한다면 consumer retry와 idempotency key를 어떻게 설계하겠습니까? 꼬리 질문: poison message가 계속 실패할 때 DLQ와 알림 기준은 어떻게 둘까요?",
                    "producer, consumer, retry, DLQ 책임 분리",
                    "Kafka consumer idempotency and dead-letter queue",
                    "주문 생성 이벤트를 발행하고 consumer가 포인트 적립을 처리하는 API를 만듭니다. TODO-STUDENT는 중복 이벤트 방지와 실패 이벤트의 DLQ 이동 조건으로 둡니다."
            );
        }

        if (containsAny(text, "oauth", "jwt", "security", "token", "authentication", "authorization")) {
            return new TopicBriefingProfile(
                    "인증과 인가",
                    "핵심은 사용자의 신원을 확인하고 권한을 검증하는 흐름을 토큰 수명, 갱신, 폐기 정책과 함께 설계하는 것입니다.",
                    "인증은 API의 입구이므로 보안성뿐 아니라 사용자 경험, 장애 대응, 감사 로그까지 함께 고려해야 합니다",
                    "지원자가 JWT 발급 코드보다 토큰 탈취, refresh rotation, 권한 변경 반영 같은 실무 질문에 답할 수 있는지 확인하기 좋습니다.",
                    "JWT access token과 refresh token을 함께 쓴다면 만료 시간, 저장 위치, rotation 정책을 어떻게 정하겠습니까? 꼬리 질문: refresh token 재사용이 감지되면 어떤 세션을 폐기하고 어떤 로그를 남기겠습니까?",
                    "token lifecycle, refresh rotation, revoke 조건",
                    "JWT refresh token rotation reuse detection",
                    "로그인/토큰 재발급 API를 만들고 refresh token rotation을 구현합니다. TODO-STUDENT는 재사용 탐지와 사용자 세션 폐기 로직으로 둡니다."
            );
        }

        return new TopicBriefingProfile(
                "백엔드 학습 소재",
                "핵심은 이 소재가 어떤 서버 문제를 해결하고, Spring Boot 프로젝트에서 어떤 작은 실험으로 검증할 수 있는지입니다.",
                "새로운 기술 소재는 이름보다 해결하는 문제, 적용 조건, 운영 trade-off를 설명할 수 있을 때 학습 가치가 생깁니다",
                "지원자가 개념을 외운 수준을 넘어 API, persistence, test, deployment 관점으로 쪼개 생각하는지 확인할 수 있습니다.",
                "이 소재를 Spring Boot 서비스에 적용한다면 어떤 계층에 두고 어떤 테스트로 검증하겠습니까? 꼬리 질문: 장애나 비용이 증가할 때 기능을 끄거나 축소하는 기준은 어떻게 잡겠습니까?",
                "문제가 발생하는 조건, 해결 방식, 적용하지 말아야 할 조건",
                "backend trade-off implementation checklist",
                "이 소재를 반영한 작은 CRUD API를 만들고, 핵심 정책 판단 로직 하나를 TODO-STUDENT로 남깁니다. 완료 기준은 정상/실패 케이스 테스트와 README에 trade-off 정리입니다."
        );
    }

    private String conciseSummary(String summary) {
        String normalized = nullToEmpty(summary).replaceAll("\\s+", " ").trim();
        if (normalized.isBlank()) {
            return "";
        }
        int maxLength = Math.min(normalized.length(), 220);
        String excerpt = normalized.substring(0, maxLength);
        if (maxLength < normalized.length()) {
            return excerpt.stripTrailing() + "...";
        }
        return excerpt;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private record TopicBriefingProfile(
            String topic,
            String summaryFocus,
            String whyItMatters,
            String backendValue,
            String interviewQuestion,
            String readingFocus,
            String researchTopic,
            String miniProjectIdea
    ) {
    }

    private Path writeMarkdown(LocalDate briefingDate, String markdown) {
        try {
            Path outputDirectory = Path.of(briefingProperties.outputDirectory());
            Files.createDirectories(outputDirectory);
            Path outputPath = outputDirectory.resolve(briefingDate + ".md");
            Files.writeString(outputPath, markdown, StandardCharsets.UTF_8);
            return outputPath;
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to write daily briefing markdown.", exception);
        }
    }
}
