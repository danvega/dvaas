package dev.danvega.dvaas.tools.beehiiv;

import dev.danvega.dvaas.tools.beehiiv.model.Post;
import dev.danvega.dvaas.tools.beehiiv.model.PublicationStats;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP tools for Beehiiv newsletter operations
 * Supports multiple publications (danvega, bytesizedai)
 */
@Component
@ConditionalOnBean(BeehiivService.class)
public class BeehiivTools {

    private final BeehiivService beehiivService;

    public BeehiivTools(BeehiivService beehiivService) {
        this.beehiivService = beehiivService;
    }

    @McpTool(name = "beehiiv-get-latest-posts",
             description = "Get the most recent newsletter posts from Dan Vega's Beehiiv publications (danvega, bytesizedai, or all)")
    public List<Post> getLatestPosts(
            @McpToolParam(description = "Publication name: 'danvega', 'bytesizedai', or 'all' (default: 'all')",
                         required = false) String publication,
            @McpToolParam(description = "Number of posts to retrieve (default: 10, max: 50)",
                         required = false) String count) {

        String pubFilter = publication != null && !publication.trim().isEmpty() ? publication.trim() : "all";
        int maxResults = parseCount(count, 10, 50);

        return beehiivService.getLatestPosts(pubFilter, maxResults);
    }

    @McpTool(name = "beehiiv-search-posts-by-keyword",
             description = "Search for newsletter posts by keyword in title, content, or authors (e.g., 'spring', 'ai', 'java')")
    public List<Post> searchPostsByKeyword(
            @McpToolParam(description = "Keyword to search for in post titles, content, and authors",
                         required = true) String keyword,
            @McpToolParam(description = "Publication name: 'danvega', 'bytesizedai', or 'all' (default: 'all')",
                         required = false) String publication,
            @McpToolParam(description = "Number of posts to retrieve (default: 10, max: 50)",
                         required = false) String count) {

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword parameter is required.");
        }

        String pubFilter = publication != null && !publication.trim().isEmpty() ? publication.trim() : "all";
        int maxResults = parseCount(count, 10, 50);

        return beehiivService.searchPostsByKeyword(pubFilter, keyword.trim(), maxResults);
    }

    @McpTool(name = "beehiiv-get-posts-by-status",
             description = "Get newsletter posts filtered by status: 'draft' (not scheduled), 'confirmed' (published/scheduled), 'archived', or 'all'")
    public List<Post> getPostsByStatus(
            @McpToolParam(description = "Post status: 'draft', 'confirmed', 'archived', or 'all' (default: 'confirmed')",
                         required = false) String status,
            @McpToolParam(description = "Publication name: 'danvega', 'bytesizedai', or 'all' (default: 'all')",
                         required = false) String publication,
            @McpToolParam(description = "Number of posts to retrieve (default: 10, max: 50)",
                         required = false) String count) {

        String statusFilter = status != null && !status.trim().isEmpty() ? status.trim() : "confirmed";
        String pubFilter = publication != null && !publication.trim().isEmpty() ? publication.trim() : "all";
        int maxResults = parseCount(count, 10, 50);

        return beehiivService.getPostsByStatus(pubFilter, statusFilter, maxResults);
    }

    @McpTool(name = "beehiiv-get-publication-stats",
             description = "Get statistics and information about Dan Vega's Beehiiv newsletter publications")
    public PublicationStats getPublicationStats(
            @McpToolParam(description = "Publication name: 'danvega', 'bytesizedai', or 'all' (default: 'all')",
                         required = false) String publication) {

        String pubFilter = publication != null && !publication.trim().isEmpty() ? publication.trim() : "all";
        return beehiivService.getPublicationStats(pubFilter);
    }

    /**
     * Parse count parameter with validation
     */
    private int parseCount(String count, int defaultValue, int maxValue) {
        if (count == null || count.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            int parsedCount = Integer.parseInt(count.trim());
            return Math.min(Math.max(parsedCount, 1), maxValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
