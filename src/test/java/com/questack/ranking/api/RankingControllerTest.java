package com.questack.ranking.api;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.questack.ranking.api.dto.RankingRunResult;
import com.questack.ranking.api.dto.RankingScoreResponse;
import com.questack.ranking.service.RankingService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RankingController.class)
@AutoConfigureRestDocs(outputDir = "docs/api-docs")
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RankingService rankingService;

    @Test
    void rankCollectedItems() throws Exception {
        when(rankingService.rankUnscoredItems())
                .thenReturn(new RankingRunResult(5, 3, 2));

        mockMvc.perform(post("/rankings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidateCount").value(5))
                .andExpect(jsonPath("$.scoredCount").value(3))
                .andExpect(jsonPath("$.skippedAlreadyScoredCount").value(2))
                .andDo(document(
                        "rankings-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("candidateCount").description("Number of collected items considered for ranking."),
                                fieldWithPath("scoredCount").description("Number of collected items newly scored."),
                                fieldWithPath("skippedAlreadyScoredCount").description("Number of collected items skipped because they already had a ranking score.")
                        )
                ));
    }

    @Test
    void findTopRankings() throws Exception {
        when(rankingService.findTopRankings(3))
                .thenReturn(List.of(new RankingScoreResponse(
                        1L,
                        10L,
                        "spring-ai-rag-service",
                        "https://github.com/example/spring-ai-rag-service",
                        12,
                        9,
                        9,
                        30,
                        "Java/JVM relevance, Spring backend relevance, AI backend retrieval topic"
                )));

        mockMvc.perform(get("/rankings/top").queryParam("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rankingScoreId").value(1))
                .andExpect(jsonPath("$[0].title").value("spring-ai-rag-service"))
                .andExpect(jsonPath("$[0].totalScore").value(30))
                .andDo(document(
                        "rankings-top",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("limit").description("Maximum number of ranking results to return. Accepted range is 1 to 10.")
                        ),
                        responseFields(
                                fieldWithPath("[].rankingScoreId").description("Ranking score identifier."),
                                fieldWithPath("[].collectedItemId").description("Collected item identifier."),
                                fieldWithPath("[].title").description("Collected item title."),
                                fieldWithPath("[].canonicalUrl").description("Canonical URL of the collected item."),
                                fieldWithPath("[].backendRelevanceScore").description("Score for backend career relevance."),
                                fieldWithPath("[].learningValueScore").description("Score for learning value."),
                                fieldWithPath("[].implementationValueScore").description("Score for hands-on implementation value."),
                                fieldWithPath("[].totalScore").description("Sum of ranking score dimensions."),
                                fieldWithPath("[].reasons").description("Human-readable reasons produced by ranking rules.")
                        )
                ));
    }
}
