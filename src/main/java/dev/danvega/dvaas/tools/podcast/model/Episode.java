package dev.danvega.dvaas.tools.podcast.model;

import java.time.LocalDateTime;

public record Episode(
        String id,
        String title,
        String description,
        String showId,
        String showTitle,
        LocalDateTime publishedAt,
        String audioUrl,
        String duration,
        String status,
        Integer season,
        Integer number
) {

    public static Episode basic(String id, String title, String showTitle, LocalDateTime publishedAt) {
        return new Episode(id, title, null, null, showTitle, publishedAt, null, null, null, null, null);
    }

    public boolean isPublished() {
        return "published".equalsIgnoreCase(status);
    }

    public boolean isScheduled() {
        return "scheduled".equalsIgnoreCase(status);
    }

    public String getShortDescription() {
        if (description == null || description.isEmpty()) {
            return "";
        }
        return description.length() > 300 ? description.substring(0, 300) + "..." : description;
    }

    public boolean hasAudio() {
        return audioUrl != null && !audioUrl.isEmpty();
    }

    public String getFormattedDuration() {
        if (duration == null || duration.isEmpty()) {
            return "Unknown duration";
        }
        // Duration comes as ISO 8601 duration format or seconds
        // This is a simple formatter, can be enhanced based on actual API response
        return duration;
    }

    public String getEpisodeIdentifier() {
        if (season != null && number != null) {
            return String.format("S%dE%d", season, number);
        } else if (number != null) {
            return String.format("Episode %d", number);
        }
        return "";
    }
}
