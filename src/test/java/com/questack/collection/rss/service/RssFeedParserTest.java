package com.questack.collection.rss.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class RssFeedParserTest {

    private final RssFeedParser parser = new RssFeedParser();

    @Test
    void parsesRssItems() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <rss version="2.0">
                    <channel>
                        <item>
                            <title>Spring Boot Cache Strategy</title>
                            <description>Redis cache invalidation in Spring Boot</description>
                            <link>https://example.com/cache</link>
                            <author>spring-team</author>
                            <pubDate>Fri, 29 May 2026 10:00:00 GMT</pubDate>
                        </item>
                    </channel>
                </rss>
                """;

        List<RssFeedItem> items = parser.parse(xml);

        assertThat(items).hasSize(1);
        assertThat(items.getFirst().title()).isEqualTo("Spring Boot Cache Strategy");
        assertThat(items.getFirst().description()).contains("Redis cache");
        assertThat(items.getFirst().link()).isEqualTo("https://example.com/cache");
        assertThat(items.getFirst().author()).isEqualTo("spring-team");
        assertThat(items.getFirst().publishedAt()).isNotNull();
    }

    @Test
    void parsesAtomEntries() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <feed xmlns="http://www.w3.org/2005/Atom">
                    <entry>
                        <title>Spring AI RAG</title>
                        <summary>RAG backend implementation</summary>
                        <link href="https://example.com/rag" />
                        <author><name>ai-team</name></author>
                        <updated>2026-05-29T10:00:00Z</updated>
                    </entry>
                </feed>
                """;

        List<RssFeedItem> items = parser.parse(xml);

        assertThat(items).hasSize(1);
        assertThat(items.getFirst().title()).isEqualTo("Spring AI RAG");
        assertThat(items.getFirst().description()).contains("RAG backend");
        assertThat(items.getFirst().link()).isEqualTo("https://example.com/rag");
        assertThat(items.getFirst().author()).isEqualTo("ai-team");
        assertThat(items.getFirst().publishedAt()).isNotNull();
    }
}
