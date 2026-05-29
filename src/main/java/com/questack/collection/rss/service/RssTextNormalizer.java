package com.questack.collection.rss.service;

import java.util.regex.Pattern;

class RssTextNormalizer {

    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private RssTextNormalizer() {
    }

    static String title(String value) {
        return truncate(plainText(value), 300);
    }

    static String summary(String value) {
        return truncate(plainText(value), 2000);
    }

    static String externalId(String value) {
        return truncate(value, 100);
    }

    static String author(String value) {
        return truncate(plainText(value), 100);
    }

    private static String plainText(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String withoutTags = HTML_TAG.matcher(value).replaceAll(" ");
        return WHITESPACE.matcher(withoutTags).replaceAll(" ").trim();
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
