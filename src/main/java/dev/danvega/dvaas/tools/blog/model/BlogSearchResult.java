package dev.danvega.dvaas.tools.blog.model;

import java.util.List;

/**
 * Represents blog search results for MCP tool responses
 */
public record BlogSearchResult(
        List<BlogPost> posts,
        String searchKeyword,
        int totalMatches,
        String searchType,
        String dateRangeDescription
) {

    /**
     * Create a search result for keyword-based searches
     */
    public static BlogSearchResult forKeyword(List<BlogPost> posts, String keyword) {
        return new BlogSearchResult(posts, keyword, posts.size(), "keyword", null);
    }

    /**
     * Create a search result for date range searches
     */
    public static BlogSearchResult forDateRange(List<BlogPost> posts, String dateRangeDescription) {
        return new BlogSearchResult(posts, null, posts.size(), "date_range", dateRangeDescription);
    }

    /**
     * Create a search result for latest posts
     */
    public static BlogSearchResult forLatest(List<BlogPost> posts) {
        return new BlogSearchResult(posts, null, posts.size(), "latest", null);
    }

    /**
     * Get a description of the search performed
     */
    public String getSearchDescription() {
        return switch (searchType) {
            case "keyword" -> String.format("keyword search for '%s'", searchKeyword);
            case "date_range" -> String.format("date range filter: %s", dateRangeDescription);
            case "latest" -> "latest posts";
            default -> "unknown search type";
        };
    }

    /**
     * Check if this search returned any results
     */
    public boolean hasResults() {
        return posts != null && !posts.isEmpty();
    }
}