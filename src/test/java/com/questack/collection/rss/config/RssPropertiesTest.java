package com.questack.collection.rss.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RssPropertiesTest {

    @Autowired
    private RssProperties rssProperties;

    @Test
    void configuresAtLeastFiveMvpTechnicalBlogFeedsWithExplicitPriorities() {
        List<RssFeedProperties> feeds = rssProperties.feeds();

        assertThat(feeds).hasSizeGreaterThanOrEqualTo(5);
        assertThat(feeds)
                .extracting(RssFeedProperties::name)
                .contains(
                        "Spring Blog",
                        "NAVER D2",
                        "Kakao Tech",
                        "AWS News Blog",
                        "InfoQ Software Engineering"
                );
        assertThat(feeds)
                .extracting(RssFeedProperties::url)
                .contains(
                        "https://aws.amazon.com/blogs/aws/feed/",
                        "https://feed.infoq.com/news/SoftwareDevelopment"
                );
        assertThat(feeds)
                .allSatisfy(feed -> assertThat(feed.priority()).isPositive());
        assertThat(feeds.stream().map(RssFeedProperties::priority).toList())
                .hasSameSizeAs(new HashSet<>(feeds.stream().map(RssFeedProperties::priority).toList()));
    }
}
