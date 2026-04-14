package dev.danvega.dvaas.tools.podcast.model;

import java.time.LocalDateTime;

public record Show(
        String id,
        String title,
        String description,
        String author,
        String websiteUrl,
        String artworkUrl,
        String status,
        LocalDateTime createdAt
) {

    public static Show basic(String id, String title, String description) {
        return new Show(id, title, description, null, null, null, null, null);
    }

    public boolean isActive() {
        return "published".equalsIgnoreCase(status);
    }

    public String getShortDescription() {
        if (description == null || description.isEmpty()) {
            return "";
        }
        return description.length() > 200 ? description.substring(0, 200) + "..." : description;
    }

    public boolean hasArtwork() {
        return artworkUrl != null && !artworkUrl.isEmpty();
    }
}
