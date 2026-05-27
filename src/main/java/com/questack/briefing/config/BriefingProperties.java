package com.questack.briefing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "briefing")
public record BriefingProperties(
        String outputDirectory
) {
}
