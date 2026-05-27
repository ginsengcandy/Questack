package com.questack.briefing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.questack.briefing.api.dto.DailyBriefingResponse;
import com.questack.briefing.config.BriefingProperties;
import com.questack.ranking.api.dto.RankingScoreResponse;
import com.questack.ranking.service.RankingService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import java.nio.file.Files;
import java.nio.file.Path;

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
                        "하네스 엔지니어링",
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
        assertThat(response.markdown()).contains("## 1. 하네스 엔지니어링");
        assertThat(response.markdown()).contains("- Source: https://example.com/harness");
        assertThat(response.markdown()).contains("- Why it matters:");
        assertThat(response.markdown()).contains("- Backend interview angle:");
        assertThat(response.markdown()).contains("- 30-minute study path:");
        assertThat(response.markdown()).contains("- Mini project idea:");
        assertThat(Files.readString(Path.of(response.filePath()))).isEqualTo(response.markdown());
    }
}
