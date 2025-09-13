package dev.danvega.dvaas.tools.youtube.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents YouTube video search results for MCP tool responses
 */
public record SearchResult(
        List<VideoInfo> videos,
        String query,
        int totalResults,
        String nextPageToken
) {

    /**
     * Create search result with basic video information
     */
    public static SearchResult of(List<VideoInfo> videos, String query) {
        return new SearchResult(videos, query, videos.size(), null);
    }

    /**
     * Get the most recent video from results
     */
    public VideoInfo getMostRecentVideo() {
        return videos.stream()
                .max((v1, v2) -> v1.publishedAt().compareTo(v2.publishedAt()))
                .orElse(null);
    }

    /**
     * Get the most popular video from results
     */
    public VideoInfo getMostPopularVideo() {
        return videos.stream()
                .max((v1, v2) -> Long.compare(v1.viewCount(), v2.viewCount()))
                .orElse(null);
    }

    /**
     * Filter videos published after a specific date
     */
    public List<VideoInfo> getVideosAfter(LocalDateTime date) {
        return videos.stream()
                .filter(video -> video.publishedAt().isAfter(date))
                .toList();
    }

    /**
     * Get summary string for the search results
     */
    public String getSummary() {
        if (videos.isEmpty()) {
            return "No videos found for query: " + query;
        }

        return String.format("Found %d videos for '%s'", totalResults, query);
    }
}