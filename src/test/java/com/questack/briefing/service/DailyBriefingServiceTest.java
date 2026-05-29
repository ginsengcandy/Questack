package com.questack.briefing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.questack.briefing.api.dto.DailyBriefingResponse;
import com.questack.briefing.config.BriefingProperties;
import com.questack.ranking.api.dto.RankingScoreResponse;
import com.questack.ranking.service.RankingService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

@ExtendWith(MockitoExtension.class)
class DailyBriefingServiceTest {

    @TempDir
    private Path tempDir;

    @Mock
    private RankingService rankingService;

    @Test
    void generatesKoreanMarkdownBriefingFromTopRankings() throws Exception {
        when(rankingService.findTopRankings(3))
                .thenReturn(List.of(new RankingScoreResponse(
                        1L,
                        10L,
                        "스마트스토어센터 Oracle에서 MySQL로의 무중단 전환기",
                        "Oracle 기반 서비스를 MySQL로 이전하며 데이터 동기화와 서비스 전환 과정을 다룬 글",
                        "https://example.com/harness",
                        10,
                        9,
                        8,
                        27,
                        "AI tool integration topic"
                )));
        DailyBriefingService service = new DailyBriefingService(
                rankingService,
                new BriefingProperties(tempDir.toString())
        );

        DailyBriefingResponse response = service.generate(LocalDate.of(2026, 5, 27));

        assertThat(response.briefingDate()).isEqualTo(LocalDate.of(2026, 5, 27));
        assertThat(response.itemCount()).isEqualTo(1);
        assertThat(response.markdown()).contains("# 데일리 브리핑: 2026-05-27");
        assertThat(response.markdown()).contains("## 1. 스마트스토어센터 Oracle에서 MySQL로의 무중단 전환기");
        assertThat(response.markdown()).contains("- Source: https://example.com/harness");
        assertThat(response.markdown()).contains("| 평가 항목 | 점수 |");
        assertThat(response.markdown()).contains("| 백엔드 적합도 | 10 |");
        assertThat(response.markdown()).contains("| 학습 가치 | 9 |");
        assertThat(response.markdown()).contains("| 구현 가치 | 8 |");
        assertThat(response.markdown()).contains("| 총점 | 27 |");
        assertThat(response.markdown()).doesNotContain("백엔드 적합도 10점, 학습 가치 9점, 구현 가치 8점");
        assertThat(response.markdown()).contains("- Summary:");
        assertThat(response.markdown()).contains("데이터 정합성");
        assertThat(response.markdown()).contains("- Why it matters:");
        assertThat(response.markdown()).contains("- Backend interview angle:");
        assertThat(response.markdown()).contains("꼬리 질문:");
        assertThat(response.markdown()).contains("- 30-minute study path:");
        assertThat(response.markdown()).contains("zero-downtime database migration rollback strategy");
        assertThat(response.markdown()).contains("- Mini project idea:");
        assertThat(response.markdown()).contains("dual-write");
        assertThat(Files.readString(Path.of(response.filePath()))).isEqualTo(response.markdown());
    }

    @Test
    void changesBriefingAdviceByTopic() {
        when(rankingService.findTopRankings(3))
                .thenReturn(List.of(
                        new RankingScoreResponse(
                                1L,
                                10L,
                                "codecentric/spring-boot-admin",
                                "Admin UI for Spring Boot applications using actuator health and metrics",
                                "https://github.com/codecentric/spring-boot-admin",
                                12,
                                8,
                                8,
                                28,
                                "Spring backend relevance, Spring Boot implementation value"
                        ),
                        new RankingScoreResponse(
                                2L,
                                11L,
                                "Spring AI 1.0.8, 1.1.7, 2.0.0-M7 Available Now",
                                "Spring AI release notes for LLM applications and RAG support",
                                "https://spring.io/blog/spring-ai",
                                9,
                                7,
                                7,
                                23,
                                "Spring backend relevance, Spring AI backend application"
                        )
                ));
        DailyBriefingService service = new DailyBriefingService(
                rankingService,
                new BriefingProperties(tempDir.toString())
        );

        DailyBriefingResponse response = service.generate(LocalDate.of(2026, 5, 29));

        assertThat(response.markdown()).contains("Actuator endpoint");
        assertThat(response.markdown()).contains("Spring AI로 RAG 검색 API");
        assertThat(response.markdown()).contains("Spring Boot Actuator health groups and endpoint exposure");
        assertThat(response.markdown()).contains("Spring AI RAG evaluation and fallback strategy");
    }
}
