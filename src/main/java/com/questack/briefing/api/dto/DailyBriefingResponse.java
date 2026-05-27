package com.questack.briefing.api.dto;

import java.time.LocalDate;

public record DailyBriefingResponse(
        LocalDate briefingDate,
        String filePath,
        int itemCount,
        String markdown
) {
}
