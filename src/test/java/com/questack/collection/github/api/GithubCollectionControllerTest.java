package com.questack.collection.github.api;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.questack.collection.github.api.dto.GithubCollectionResult;
import com.questack.collection.github.service.GithubCollector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GithubCollectionController.class)
@AutoConfigureRestDocs(outputDir = "src/docs/api-docs")
class GithubCollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GithubCollector githubCollector;

    @Test
    void collectGithubRepositories() throws Exception {
        when(githubCollector.collect(anyString(), anyInt()))
                .thenReturn(new GithubCollectionResult(3, 2, 1));

        mockMvc.perform(post("/collections/github")
                        .queryParam("query", "language:Java topic:spring-boot stars:>=100 fork:false")
                        .queryParam("perPage", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fetchedCount").value(3))
                .andExpect(jsonPath("$.savedCount").value(2))
                .andExpect(jsonPath("$.skippedDuplicateCount").value(1))
                .andDo(document(
                        "collections-github",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("query").description("GitHub repository search query."),
                                parameterWithName("perPage").description("Number of repositories to fetch. Accepted range is 1 to 30.")
                        ),
                        responseFields(
                                fieldWithPath("fetchedCount").description("Number of repositories fetched from GitHub."),
                                fieldWithPath("savedCount").description("Number of newly saved collected items."),
                                fieldWithPath("skippedDuplicateCount").description("Number of repositories skipped because their canonical URL already exists.")
                        )
                ));
    }
}
