package dev.danvega.dvaas.tools.blog;

import dev.danvega.dvaas.tools.blog.model.BlogPost;
import dev.danvega.dvaas.tools.blog.model.BlogSearchResult;
import dev.danvega.dvaas.tools.blog.model.BlogStats;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * MCP tools for blog RSS feed operations
 */
@Component
@ConditionalOnBean(BlogService.class)
public class BlogTools {

    private final BlogService blogService;

    public BlogTools(BlogService blogService) {
        this.blogService = blogService;
    }

    @McpTool(name = "blog-get-latest-posts",
             description = "Get the most recent blog posts from Dan Vega's blog")
    public String getLatestPosts(
            @McpToolParam(description = "Number of posts to retrieve (default: 10, max: 50)",
                         required = false) String count) {

        int maxResults = parseCount(count, 10, 50);

        try {
            List<BlogPost> posts = blogService.getLatestPosts(maxResults);

            if (posts.isEmpty()) {
                return "No recent blog posts found.";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Latest %d blog posts from Dan Vega's blog:\n\n", posts.size()));

            for (int i = 0; i < posts.size(); i++) {
                BlogPost post = posts.get(i);
                result.append(String.format("%d. **%s**\n", i + 1, post.title()));
                result.append(String.format("   📅 Published: %s\n",
                    post.publishedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
                result.append(String.format("   🔗 URL: %s\n", post.getFullUrl()));

                if (post.hasYouTubeVideo()) {
                    result.append(String.format("   🎥 YouTube: %s\n", post.youtubeVideoUrl()));
                }

                String shortDesc = post.getShortDescription();
                if (!shortDesc.isEmpty()) {
                    result.append(String.format("   📝 %s\n", shortDesc));
                }

                List<String> tags = post.extractPotentialTags();
                if (!tags.isEmpty()) {
                    result.append(String.format("   🏷️ Tags: %s\n", String.join(", ", tags)));
                }

                result.append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching latest blog posts: " + e.getMessage();
        }
    }

    @McpTool(name = "blog-search-posts-by-keyword",
             description = "Search for blog posts by keyword in title or description (e.g., 'spring boot', 'ai', 'graphql')")
    public String searchPostsByKeyword(
            @McpToolParam(description = "Keyword to search for in post titles and descriptions",
                         required = true) String keyword,
            @McpToolParam(description = "Number of posts to retrieve (default: 10, max: 50)",
                         required = false) String count) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return "Error: Keyword parameter is required.";
        }

        int maxResults = parseCount(count, 10, 50);

        try {
            BlogSearchResult searchResult = blogService.searchPostsByKeyword(keyword.trim(), maxResults);

            if (!searchResult.hasResults()) {
                return String.format("No blog posts found for keyword: '%s'", keyword);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Found %d blog posts matching '%s':\n\n",
                searchResult.totalMatches(), keyword));

            for (int i = 0; i < searchResult.posts().size(); i++) {
                BlogPost post = searchResult.posts().get(i);
                result.append(String.format("%d. **%s**\n", i + 1, post.title()));
                result.append(String.format("   📅 Published: %s\n",
                    post.publishedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
                result.append(String.format("   🔗 URL: %s\n", post.getFullUrl()));

                if (post.hasYouTubeVideo()) {
                    result.append(String.format("   🎥 YouTube: %s\n", post.youtubeVideoUrl()));
                }

                String shortDesc = post.getShortDescription();
                if (!shortDesc.isEmpty()) {
                    result.append(String.format("   📝 %s\n", shortDesc));
                }

                result.append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "Error searching blog posts: " + e.getMessage();
        }
    }

    @McpTool(name = "blog-get-posts-by-date-range",
             description = "Get blog posts within a specific date range or year (e.g., '2024', '2023-01-01 to 2023-12-31')")
    public String getPostsByDateRange(
            @McpToolParam(description = "Date range: '2024' for year, or 'YYYY-MM-DD to YYYY-MM-DD' for custom range",
                         required = true) String dateRange,
            @McpToolParam(description = "Number of posts to retrieve (default: 10, max: 50)",
                         required = false) String count) {

        if (dateRange == null || dateRange.trim().isEmpty()) {
            return "Error: Date range parameter is required. Use format '2024' or '2023-01-01 to 2023-12-31'.";
        }

        int maxResults = parseCount(count, 10, 50);

        try {
            BlogSearchResult searchResult = parseDateRangeAndSearch(dateRange.trim(), maxResults);

            if (!searchResult.hasResults()) {
                return String.format("No blog posts found for date range: %s", dateRange);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Found %d blog posts in %s:\n\n",
                searchResult.totalMatches(), searchResult.dateRangeDescription()));

            for (int i = 0; i < searchResult.posts().size(); i++) {
                BlogPost post = searchResult.posts().get(i);
                result.append(String.format("%d. **%s**\n", i + 1, post.title()));
                result.append(String.format("   📅 Published: %s\n",
                    post.publishedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
                result.append(String.format("   🔗 URL: %s\n", post.getFullUrl()));

                if (post.hasYouTubeVideo()) {
                    result.append(String.format("   🎥 YouTube: %s\n", post.youtubeVideoUrl()));
                }

                result.append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching posts by date range: " + e.getMessage();
        }
    }

    @McpTool(name = "blog-get-stats",
             description = "Get overall statistics and information about Dan Vega's blog")
    public String getBlogStats() {
        try {
            BlogStats stats = blogService.getBlogStats();

            if (stats.totalPosts() == 0) {
                return "No blog statistics available - no posts found.";
            }

            StringBuilder result = new StringBuilder();
            result.append("**Dan Vega's Blog Statistics**\n\n");
            result.append(String.format("📝 Total Posts: %,d\n", stats.totalPosts()));
            result.append(String.format("📅 Blog Timespan: %s\n", stats.getBlogTimespan()));
            result.append(String.format("📊 Posting Frequency: %s\n", stats.getPostingFrequency()));
            result.append(String.format("📈 Average Posts/Month: %.1f\n", stats.averagePostsPerMonth()));
            result.append(String.format("🗓️ Posts This Year: %,d\n", stats.postsThisYear()));
            result.append(String.format("📆 Posts This Month: %,d\n", stats.postsThisMonth()));

            if (stats.postsWithYouTubeVideos() > 0) {
                result.append(String.format("🎥 Posts with YouTube Videos: %,d (%s)\n",
                    stats.postsWithYouTubeVideos(), stats.getYouTubeIntegrationPercentage()));
            }

            if (stats.mostCommonTag() != null) {
                result.append(String.format("🏷️ Most Common Tag: %s\n", stats.mostCommonTag()));
            }

            if (stats.firstPostDate() != null) {
                result.append(String.format("\n📅 First Post: %s\n",
                    stats.firstPostDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
            }

            if (stats.latestPostDate() != null) {
                result.append(String.format("📅 Latest Post: %s\n",
                    stats.latestPostDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching blog statistics: " + e.getMessage();
        }
    }

    /**
     * Parse date range input and perform search
     */
    private BlogSearchResult parseDateRangeAndSearch(String dateRange, int maxResults) {
        // Handle year-only format (e.g., "2024")
        if (dateRange.matches("\\d{4}")) {
            int year = Integer.parseInt(dateRange);
            return blogService.getPostsByYear(year, maxResults);
        }

        // Handle range format (e.g., "2023-01-01 to 2023-12-31")
        if (dateRange.contains(" to ")) {
            String[] parts = dateRange.split(" to ");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid date range format. Use 'YYYY-MM-DD to YYYY-MM-DD'.");
            }

            try {
                LocalDateTime startDate = LocalDateTime.parse(parts[0].trim() + "T00:00:00");
                LocalDateTime endDate = LocalDateTime.parse(parts[1].trim() + "T23:59:59");
                return blogService.getPostsByDateRange(startDate, endDate, maxResults);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD format.");
            }
        }

        // Handle single date format (e.g., "2023-12-25")
        if (dateRange.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                LocalDateTime singleDate = LocalDateTime.parse(dateRange + "T00:00:00");
                LocalDateTime endOfDay = singleDate.plusDays(1).minusSeconds(1);
                return blogService.getPostsByDateRange(singleDate, endOfDay, maxResults);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD format.");
            }
        }

        throw new IllegalArgumentException("Invalid date range format. Use '2024' or '2023-01-01 to 2023-12-31'.");
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