package com.questack.briefing.api;

import com.questack.briefing.api.dto.DailyBriefingResponse;
import com.questack.briefing.service.DailyBriefingService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DailyBriefingController {

    private final DailyBriefingService dailyBriefingService;

    public DailyBriefingController(DailyBriefingService dailyBriefingService) {
        this.dailyBriefingService = dailyBriefingService;
    }

    @PostMapping("/briefings/daily")
    public DailyBriefingResponse generateDailyBriefing(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate briefingDate = date == null ? LocalDate.now() : date;
        return dailyBriefingService.generate(briefingDate);
    }
}
