package dev.danvega.dvaas.tools.youtube.model;

import java.time.LocalDateTime;

/**
 * Represents YouTube video information for MCP tool responses
 */
public record VideoInfo(
        String id,
        String title,
        String url,
        String description,
        LocalDateTime publishedAt,
        long viewCount,
        long likeCount,
        long commentCount,
        String duration,
        String thumbnailUrl
) {

    /**
     * Create a simplified VideoInfo for basic responses
     */
    public static VideoInfo basic(String id, String title, String url, LocalDateTime publishedAt, long viewCount) {
        return new VideoInfo(id, title, url, null, publishedAt, viewCount, 0, 0, null, null);
    }

    /**
     * Get formatted view count for display
     */
    public String getFormattedViewCount() {
        if (viewCount >= 1_000_000) {
            return String.format("%.1fM", viewCount / 1_000_000.0);
        } else if (viewCount >= 1_000) {
            return String.format("%.1fK", viewCount / 1_000.0);
        }
        return String.valueOf(viewCount);
    }

    /**
     * Get YouTube video URL
     */
    public String getYouTubeUrl() {
        return url != null ? url : "https://www.youtube.com/watch?v=" + id;
    }
}