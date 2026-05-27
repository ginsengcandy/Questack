package com.questack.briefing.api;

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

import com.questack.briefing.api.dto.DailyBriefingResponse;
import com.questack.briefing.service.DailyBriefingService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DailyBriefingController.class)
@AutoConfigureRestDocs(outputDir = "docs/api-docs")
class DailyBriefingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DailyBriefingService dailyBriefingService;

    @Test
    void generateDailyBriefing() throws Exception {
        LocalDate briefingDate = LocalDate.of(2026, 5, 27);
        when(dailyBriefingService.generate(briefingDate))
                .thenReturn(new DailyBriefingResponse(
                        briefingDate,
                        "docs/daily-briefings/2026-05-27.md",
                        3,
                        "# 데일리 브리핑: 2026-05-27\n"
                ));

        mockMvc.perform(post("/briefings/daily").queryParam("date", "2026-05-27"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.briefingDate").value("2026-05-27"))
                .andExpect(jsonPath("$.filePath").value("docs/daily-briefings/2026-05-27.md"))
                .andExpect(jsonPath("$.itemCount").value(3))
                .andDo(document(
                        "briefings-daily-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("date").optional().description("Briefing date in ISO-8601 format. Defaults to the current date.")
                        ),
                        responseFields(
                                fieldWithPath("briefingDate").description("Date used to generate the daily briefing."),
                                fieldWithPath("filePath").description("Markdown file path written by the server."),
                                fieldWithPath("itemCount").description("Number of ranking items included in the briefing."),
                                fieldWithPath("markdown").description("Generated Korean Markdown briefing content.")
                        )
                ));
    }
}
