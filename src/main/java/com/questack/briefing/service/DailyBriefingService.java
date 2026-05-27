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

    private String whyItMatters(RankingScoreResponse ranking) {
        return "%s 항목은 백엔드 적합도 %d점, 학습 가치 %d점, 구현 가치 %d점으로 평가되었습니다. %s. "
                .formatted(
                        ranking.title(),
                        ranking.backendRelevanceScore(),
                        ranking.learningValueScore(),
                        ranking.implementationValueScore(),
                        ranking.reasons()
                )
                + "신입 백엔드 개발자에게 중요한 것은 단순히 새로운 기술명을 아는 것이 아니라, 해당 기술이 API, 데이터 저장, 인증, 메시징, 운영 자동화 같은 서버 개발 문제를 어떻게 해결하는지 설명하고 작게 구현해보는 것입니다.";
    }

    private String backendInterviewAngle(RankingScoreResponse ranking) {
        return "면접에서는 이 주제를 '왜 필요한가', '기존 방식과 어떤 trade-off가 있는가', 'Spring 기반 서비스에 적용한다면 어떤 계층과 테스트가 필요한가'로 질문받을 수 있습니다. "
                + "최소한 %s의 핵심 개념, 적용 시나리오, 장애나 비용 관점의 주의점을 본인 언어로 설명할 수 있어야 합니다."
                .formatted(ranking.title());
    }

    private String studyPath(RankingScoreResponse ranking) {
        return "10분 동안 저장소 README와 주요 패키지 구조를 훑고, 10분 동안 %s가 해결하려는 문제를 정리한 뒤, 마지막 10분 동안 Spring Boot 프로젝트에 적용 가능한 가장 작은 실습 단위를 TODO로 적어봅니다. 참고 URL: %s"
                .formatted(ranking.title(), ranking.canonicalUrl());
    }

    private String miniProjectIdea(RankingScoreResponse ranking) {
        return "Questack 미니 프로젝트로 `%s` 주제를 반영한 작은 Spring Boot API를 만들고, 핵심 로직 1곳은 `TODO-STUDENT`로 남겨 직접 구현합니다. 완료 기준은 테스트 통과와 GitHub push입니다."
                .formatted(ranking.title());
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
