package dev.danvega.dvaas.tools.youtube;

import dev.danvega.dvaas.tools.youtube.model.ChannelStats;
import dev.danvega.dvaas.tools.youtube.model.SearchResult;
import dev.danvega.dvaas.tools.youtube.model.VideoInfo;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MCP tools for YouTube channel operations
 */
@Component
@ConditionalOnBean(YouTubeService.class)
public class YouTubeTools {

    private final YouTubeService youTubeService;

    public YouTubeTools(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    @McpTool(name = "youtube-get-latest-videos",
             description = "Get the most recent videos from Dan Vega's YouTube channel")
    public String getLatestVideos(
            @McpToolParam(description = "Number of videos to retrieve (default: 10, max: 50)",
                         required = false) String count) {

        int maxResults = parseCount(count, 10, 50);

        try {
            List<VideoInfo> videos = youTubeService.getLatestVideos(maxResults);

            if (videos.isEmpty()) {
                return "No recent videos found.";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Latest %d videos from Dan Vega's YouTube channel:\n\n", videos.size()));

            for (int i = 0; i < videos.size(); i++) {
                VideoInfo video = videos.get(i);
                result.append(String.format("%d. **%s**\n", i + 1, video.title()));
                result.append(String.format("   📅 Published: %s\n",
                    video.publishedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
                result.append(String.format("   🔗 URL: %s\n", video.getYouTubeUrl()));
                if (video.viewCount() > 0) {
                    result.append(String.format("   👁️ Views: %s\n", video.getFormattedViewCount()));
                }
                result.append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching latest videos: " + e.getMessage();
        }
    }

    @McpTool(name = "youtube-get-top-videos",
             description = "Get the top-performing videos from Dan Vega's YouTube channel by view count")
    public String getTopVideos(
            @McpToolParam(description = "Number of videos to retrieve (default: 10, max: 50)",
                         required = false) String count,
            @McpToolParam(description = "Time range: 'recent', 'month', 'year', 'all' (default: 'recent')",
                         required = false) String timeRange) {

        int maxResults = parseCount(count, 10, 50);
        String range = timeRange != null ? timeRange.toLowerCase() : "recent";

        try {
            List<VideoInfo> videos = youTubeService.getTopVideos(maxResults, range);

            if (videos.isEmpty()) {
                return "No top videos found.";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Top %d performing videos from Dan Vega's YouTube channel:\n\n", videos.size()));

            for (int i = 0; i < videos.size(); i++) {
                VideoInfo video = videos.get(i);
                result.append(String.format("%d. **%s**\n", i + 1, video.title()));
                result.append(String.format("   👁️ Views: %s\n", video.getFormattedViewCount()));
                result.append(String.format("   📅 Published: %s\n",
                    video.publishedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
                result.append(String.format("   🔗 URL: %s\n", video.getYouTubeUrl()));
                if (video.likeCount() > 0) {
                    result.append(String.format("   👍 Likes: %,d\n", video.likeCount()));
                }
                result.append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching top videos: " + e.getMessage();
        }
    }

    @McpTool(name = "youtube-search-videos-by-topic",
             description = "Search for videos on Dan Vega's YouTube channel by topic or keyword (e.g., 'java', 'spring', 'spring-ai')")
    public String searchVideosByTopic(
            @McpToolParam(description = "Topic or keyword to search for (e.g., 'java', 'spring', 'spring-ai')",
                         required = true) String topic,
            @McpToolParam(description = "Number of videos to retrieve (default: 10, max: 50)",
                         required = false) String count) {

        int maxResults = parseCount(count, 10, 50);

        if (topic == null || topic.trim().isEmpty()) {
            return "Error: Topic parameter is required.";
        }

        try {
            SearchResult searchResult = youTubeService.searchVideosByTopic(topic.trim(), maxResults);

            if (searchResult.videos().isEmpty()) {
                return String.format("No videos found for topic: '%s'", topic);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Found %d videos about '%s' on Dan Vega's YouTube channel:\n\n",
                searchResult.videos().size(), topic));

            for (int i = 0; i < searchResult.videos().size(); i++) {
                VideoInfo video = searchResult.videos().get(i);
                result.append(String.format("%d. **%s**\n", i + 1, video.title()));
                result.append(String.format("   📅 Published: %s\n",
                    video.publishedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
                result.append(String.format("   🔗 URL: %s\n", video.getYouTubeUrl()));
                result.append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "Error searching videos: " + e.getMessage();
        }
    }

    @McpTool(name = "youtube-get-channel-stats",
             description = "Get overall statistics and information about Dan Vega's YouTube channel")
    public String getChannelStats() {
        try {
            ChannelStats stats = youTubeService.getChannelStats();

            StringBuilder result = new StringBuilder();
            result.append("**Dan Vega's YouTube Channel Statistics**\n\n");
            result.append(String.format("📺 Channel: %s\n", stats.title()));
            result.append(String.format("👥 Subscribers: %s\n", stats.getFormattedSubscriberCount()));
            result.append(String.format("👁️ Total Views: %s\n", stats.getFormattedTotalViewCount()));
            result.append(String.format("🎬 Total Videos: %,d\n", stats.videoCount()));
            result.append(String.format("📊 Average Views per Video: %,d\n", stats.getAverageViewsPerVideo()));
            result.append(String.format("📅 Channel Created: %s\n",
                stats.channelCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));

            if (stats.description() != null && !stats.description().isEmpty()) {
                String description = stats.description();
                if (description.length() > 200) {
                    description = description.substring(0, 200) + "...";
                }
                result.append(String.format("\n📝 Description: %s\n", description));
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching channel statistics: " + e.getMessage();
        }
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