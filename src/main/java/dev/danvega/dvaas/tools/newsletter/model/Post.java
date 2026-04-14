package dev.danvega.dvaas.tools.newsletter.model;

import java.time.LocalDateTime;
import java.util.List;

public record Post(
        String id,
        String publicationId,
        String publicationName,
        String title,
        List<String> authors,
        String status,
        LocalDateTime publishDate,
        LocalDateTime displayedDate,
        String webUrl,
        String thumbnailUrl,
        String contentPreview,
        String platform,
        String audience,
        List<String> contentTags,
        PostStats stats
) {

    public static Post basic(
            String id,
            String publicationId,
            String publicationName,
            String title,
            List<String> authors,
            String status,
            LocalDateTime publishDate,
            String webUrl) {
        return new Post(
                id,
                publicationId,
                publicationName,
                title,
                authors,
                status,
                publishDate,
                null,
                webUrl,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public boolean isPublished() {
        return "confirmed".equalsIgnoreCase(status);
    }

    public boolean isDraft() {
        return "draft".equalsIgnoreCase(status);
    }

    public boolean isArchived() {
        return "archived".equalsIgnoreCase(status);
    }

    public String getAuthorsFormatted() {
        if (authors == null || authors.isEmpty()) {
            return "Unknown";
        }
        return String.join(", ", authors);
    }

    public LocalDateTime getEffectivePublishDate() {
        return displayedDate != null ? displayedDate : publishDate;
    }
}
