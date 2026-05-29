package com.questack.collection.rss.service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Component
public class RssFeedParser {

    public List<RssFeedItem> parse(String xml) {
        Document document = parseDocument(xml);
        List<RssFeedItem> rssItems = parseRssItems(document);
        if (!rssItems.isEmpty()) {
            return rssItems;
        }
        return parseAtomEntries(document);
    }

    private Document parseDocument(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setExpandEntityReferences(false);
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to parse RSS/Atom feed.", exception);
        }
    }

    private List<RssFeedItem> parseRssItems(Document document) {
        NodeList itemNodes = document.getElementsByTagName("item");
        List<RssFeedItem> items = new ArrayList<>();

        for (int index = 0; index < itemNodes.getLength(); index++) {
            Element itemElement = (Element) itemNodes.item(index);
            String link = textOf(itemElement, "link");
            if (link.isBlank()) {
                link = textOf(itemElement, "guid");
            }
            items.add(new RssFeedItem(
                    textOf(itemElement, "title"),
                    textOf(itemElement, "description"),
                    link,
                    firstNonBlank(textOf(itemElement, "author"), textOf(itemElement, "dc:creator")),
                    parsePublishedAt(firstNonBlank(textOf(itemElement, "pubDate"), textOf(itemElement, "published")))
            ));
        }

        return items;
    }

    private List<RssFeedItem> parseAtomEntries(Document document) {
        NodeList entryNodes = document.getElementsByTagName("entry");
        List<RssFeedItem> items = new ArrayList<>();

        for (int index = 0; index < entryNodes.getLength(); index++) {
            Element entryElement = (Element) entryNodes.item(index);
            items.add(new RssFeedItem(
                    textOf(entryElement, "title"),
                    firstNonBlank(textOf(entryElement, "summary"), textOf(entryElement, "content")),
                    atomLink(entryElement),
                    textOf(entryElement, "name"),
                    parsePublishedAt(firstNonBlank(textOf(entryElement, "published"), textOf(entryElement, "updated")))
            ));
        }

        return items;
    }

    private String textOf(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return "";
        }
        Node node = nodes.item(0);
        return node.getTextContent() == null ? "" : node.getTextContent().trim();
    }

    private String atomLink(Element entryElement) {
        NodeList links = entryElement.getElementsByTagName("link");
        for (int index = 0; index < links.getLength(); index++) {
            Element linkElement = (Element) links.item(index);
            String rel = linkElement.getAttribute("rel");
            if (rel.isBlank() || "alternate".equals(rel)) {
                String href = linkElement.getAttribute("href");
                if (!href.isBlank()) {
                    return href;
                }
            }
        }
        return "";
    }

    private Instant parsePublishedAt(String value) {
        if (value.isBlank()) {
            return null;
        }

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.RFC_1123_DATE_TIME,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME,
                DateTimeFormatter.ISO_ZONED_DATE_TIME
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return ZonedDateTime.parse(value, formatter).toInstant();
            } catch (DateTimeParseException ignored) {
                try {
                    return OffsetDateTime.parse(value, formatter).toInstant();
                } catch (DateTimeParseException ignoredAgain) {
                    // Try the next formatter.
                }
            }
        }
        return null;
    }

    private String firstNonBlank(String first, String second) {
        return first == null || first.isBlank() ? nullToEmpty(second) : first;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
