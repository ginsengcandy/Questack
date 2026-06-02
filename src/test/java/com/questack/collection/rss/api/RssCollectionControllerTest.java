package com.questack.collection.rss.api;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.questack.collection.rss.api.dto.RssCollectionResult;
import com.questack.collection.rss.api.dto.RssFeedFailure;
import com.questack.collection.rss.service.RssCollector;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RssCollectionController.class)
@AutoConfigureRestDocs(outputDir = "docs/api-docs")
class RssCollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RssCollector rssCollector;

    @Test
    void collectRssFeeds() throws Exception {
        when(rssCollector.collect())
                .thenReturn(new RssCollectionResult(
                        3,
                        12,
                        10,
                        2,
                        1,
                        List.of(new RssFeedFailure("Broken Feed", "https://example.com/broken.xml", "500 Internal Server Error"))
                ));

        mockMvc.perform(post("/collections/rss"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedCount").value(3))
                .andExpect(jsonPath("$.fetchedCount").value(12))
                .andExpect(jsonPath("$.savedCount").value(10))
                .andExpect(jsonPath("$.skippedDuplicateCount").value(2))
                .andExpect(jsonPath("$.failedFeedCount").value(1))
                .andExpect(jsonPath("$.failedFeeds[0].feedName").value("Broken Feed"))
                .andDo(document(
                        "collections-rss",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("feedCount").description("Number of configured RSS/Atom feeds processed."),
                                fieldWithPath("fetchedCount").description("Number of items fetched from all feeds."),
                                fieldWithPath("savedCount").description("Number of newly saved collected items."),
                                fieldWithPath("skippedDuplicateCount").description("Number of feed items skipped because their canonical URL already exists or is blank."),
                                fieldWithPath("failedFeedCount").description("Number of feeds that failed while other feeds continued processing."),
                                fieldWithPath("failedFeeds").description("Failed feed details."),
                                fieldWithPath("failedFeeds[].feedName").description("Configured name of the failed feed."),
                                fieldWithPath("failedFeeds[].feedUrl").description("Configured URL of the failed feed."),
                                fieldWithPath("failedFeeds[].reason").description("Failure reason captured from the feed request or parser.")
                        )
                ));
    }
}
