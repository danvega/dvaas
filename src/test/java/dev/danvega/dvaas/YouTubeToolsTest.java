package dev.danvega.dvaas;

import dev.danvega.dvaas.tools.youtube.YouTubeService;
import dev.danvega.dvaas.tools.youtube.YouTubeTools;
import dev.danvega.dvaas.tools.youtube.model.ChannelStats;
import dev.danvega.dvaas.tools.youtube.model.SearchResult;
import dev.danvega.dvaas.tools.youtube.model.VideoInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for YouTubeTools MCP functionality with mocked YouTubeService.
 * Tests the tool output formatting and parameter handling.
 */
@SpringBootTest
@ActiveProfiles("test")
class YouTubeToolsTest {

    @Autowired
    private YouTubeTools youTubeTools;

    @MockitoBean
    private YouTubeService youTubeService;

    @Test
    void testGetLatestVideosWithDefaultCount() {
        // Given - mock latest videos
        List<VideoInfo> mockVideos = List.of(
            VideoInfo.basic("video1", "Spring Boot 3.2 Released",
                          "https://youtube.com/watch?v=video1",
                          LocalDateTime.now().minusDays(1), 2500L),
            VideoInfo.basic("video2", "Java 21 Virtual Threads",
                          "https://youtube.com/watch?v=video2",
                          LocalDateTime.now().minusDays(2), 1800L)
        );

        when(youTubeService.getLatestVideos(10)).thenReturn(mockVideos);

        // When
        String result = youTubeTools.getLatestVideos(null);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Latest 2 videos from Dan Vega's YouTube channel:"));
        assertTrue(result.contains("Spring Boot 3.2 Released"));
        assertTrue(result.contains("Java 21 Virtual Threads"));
        assertTrue(result.contains("https://youtube.com/watch?v=video1"));
        assertTrue(result.contains("2.5K")); // Formatted view count
    }

    @Test
    void testGetLatestVideosWithCustomCount() {
        // Given - mock 3 videos
        List<VideoInfo> mockVideos = List.of(
            VideoInfo.basic("v1", "Video 1", "https://youtube.com/watch?v=v1",
                          LocalDateTime.now().minusDays(1), 1000L),
            VideoInfo.basic("v2", "Video 2", "https://youtube.com/watch?v=v2",
                          LocalDateTime.now().minusDays(2), 2000L),
            VideoInfo.basic("v3", "Video 3", "https://youtube.com/watch?v=v3",
                          LocalDateTime.now().minusDays(3), 3000L)
        );

        when(youTubeService.getLatestVideos(3)).thenReturn(mockVideos);

        // When
        String result = youTubeTools.getLatestVideos("3");

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Latest 3 videos"));
        assertTrue(result.contains("Video 1"));
        assertTrue(result.contains("Video 2"));
        assertTrue(result.contains("Video 3"));
    }

    @Test
    void testGetLatestVideosEmpty() {
        // Given - no videos
        when(youTubeService.getLatestVideos(anyInt())).thenReturn(List.of());

        // When
        String result = youTubeTools.getLatestVideos("5");

        // Then
        assertEquals("No recent videos found.", result);
    }

    @Test
    void testGetTopVideos() {
        // Given - mock top videos with detailed stats
        List<VideoInfo> mockVideos = List.of(
            new VideoInfo("top1", "Most Popular Spring Tutorial",
                        "https://youtube.com/watch?v=top1",
                        null,
                        LocalDateTime.now().minusMonths(2),
                        150000L, 5000L, 500L, "PT15M30S", null),
            new VideoInfo("top2", "Java Performance Tips",
                        "https://youtube.com/watch?v=top2",
                        null,
                        LocalDateTime.now().minusMonths(1),
                        85000L, 3200L, 350L, "PT12M45S", null)
        );

        when(youTubeService.getTopVideos(5, "recent")).thenReturn(mockVideos);

        // When
        String result = youTubeTools.getTopVideos("5", "recent");

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Top 2 performing videos"));
        assertTrue(result.contains("Most Popular Spring Tutorial"));
        assertTrue(result.contains("150.0K")); // Formatted view count
        assertTrue(result.contains("5,000")); // Like count
        assertTrue(result.contains("👁️ Views:"));
        assertTrue(result.contains("👍 Likes:"));
    }

    @Test
    void testGetTopVideosWithDefaultParams() {
        // Given
        List<VideoInfo> mockVideos = List.of(
            VideoInfo.basic("v1", "Top Video", "https://youtube.com/watch?v=v1",
                          LocalDateTime.now(), 50000L)
        );

        when(youTubeService.getTopVideos(10, "recent")).thenReturn(mockVideos);

        // When
        String result = youTubeTools.getTopVideos(null, null);

        // Then
        assertTrue(result.contains("Top 1 performing videos"));
        assertTrue(result.contains("Top Video"));
    }

    @Test
    void testSearchVideosByTopic() {
        // Given - search results for "spring"
        List<VideoInfo> mockVideos = List.of(
            VideoInfo.basic("spring1", "Spring Security Deep Dive",
                          "https://youtube.com/watch?v=spring1",
                          LocalDateTime.now().minusWeeks(1), 8000L),
            VideoInfo.basic("spring2", "Spring Boot Testing",
                          "https://youtube.com/watch?v=spring2",
                          LocalDateTime.now().minusWeeks(2), 6500L)
        );

        SearchResult mockResult = new SearchResult(mockVideos, "spring", 2, null);
        when(youTubeService.searchVideosByTopic("spring", 10)).thenReturn(mockResult);

        // When
        String result = youTubeTools.searchVideosByTopic("spring", null);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Found 2 videos about 'spring'"));
        assertTrue(result.contains("Spring Security Deep Dive"));
        assertTrue(result.contains("Spring Boot Testing"));
        assertTrue(result.contains("https://youtube.com/watch?v=spring1"));
    }

    @Test
    void testSearchVideosByTopicWithCustomCount() {
        // Given
        List<VideoInfo> mockVideos = List.of(
            VideoInfo.basic("java1", "Java 21 Features", "https://youtube.com/watch?v=java1",
                          LocalDateTime.now(), 5000L)
        );

        SearchResult mockResult = new SearchResult(mockVideos, "java", 1, null);
        when(youTubeService.searchVideosByTopic("java", 5)).thenReturn(mockResult);

        // When
        String result = youTubeTools.searchVideosByTopic("java", "5");

        // Then
        assertTrue(result.contains("Found 1 videos about 'java'"));
        assertTrue(result.contains("Java 21 Features"));
    }

    @Test
    void testSearchVideosNoResults() {
        // Given - no results
        SearchResult emptyResult = new SearchResult(List.of(), "nonexistent", 0, null);
        when(youTubeService.searchVideosByTopic("nonexistent", 10)).thenReturn(emptyResult);

        // When
        String result = youTubeTools.searchVideosByTopic("nonexistent", null);

        // Then
        assertEquals("No videos found for topic: 'nonexistent'", result);
    }

    @Test
    void testSearchVideosMissingTopic() {
        // When - null topic
        String result1 = youTubeTools.searchVideosByTopic(null, "10");

        // When - empty topic
        String result2 = youTubeTools.searchVideosByTopic("", "10");

        // When - whitespace only topic
        String result3 = youTubeTools.searchVideosByTopic("   ", "10");

        // Then
        assertEquals("Error: Topic parameter is required.", result1);
        assertEquals("Error: Topic parameter is required.", result2);
        assertEquals("Error: Topic parameter is required.", result3);
    }

    @Test
    void testGetChannelStats() {
        // Given - mock channel stats
        ChannelStats mockStats = new ChannelStats(
                "UC123456789",
                "Dan Vega",
                "Spring Boot tutorials and Java programming content for developers who want to level up their skills.",
                75000L,
                2500000L,
                150L,
                LocalDateTime.of(2019, 3, 15, 10, 0),
                false
        );

        when(youTubeService.getChannelStats()).thenReturn(mockStats);

        // When
        String result = youTubeTools.getChannelStats();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("**Dan Vega's YouTube Channel Statistics**"));
        assertTrue(result.contains("📺 Channel: Dan Vega"));
        assertTrue(result.contains("👥 Subscribers: 75.0K"));
        assertTrue(result.contains("👁️ Total Views: 2.5M"));
        assertTrue(result.contains("🎬 Total Videos: 150"));
        assertTrue(result.contains("📊 Average Views per Video: 16,666"));
        assertTrue(result.contains("📅 Channel Created: Mar 15, 2019"));
        assertTrue(result.contains("📝 Description: Spring Boot tutorials")); // Truncated description
    }

    @Test
    void testGetChannelStatsWithLongDescription() {
        // Given - stats with very long description (definitely over 200 chars)
        String longDescription = "This is a very long channel description that exceeds 200 characters and should be truncated by the tool to ensure the output remains readable and doesn't overwhelm users with too much information at once. Here is even more text to make sure we are definitely over the 200 character limit for truncation.";

        ChannelStats mockStats = new ChannelStats(
                "UC123", "Test Channel", longDescription,
                1000L, 50000L, 25L,
                LocalDateTime.now().minusYears(1), false
        );

        when(youTubeService.getChannelStats()).thenReturn(mockStats);

        // When
        String result = youTubeTools.getChannelStats();

        // Then
        assertTrue(result.contains("📝 Description:"));
        assertTrue(result.contains("...")); // Should be truncated
        // Verify the displayed description is shorter than the original
        String displayedDescription = result.substring(result.indexOf("📝 Description:") + "📝 Description: ".length());
        displayedDescription = displayedDescription.substring(0, displayedDescription.indexOf('\n')).trim();
        assertTrue(displayedDescription.length() < longDescription.length(),
                  "Displayed description should be shorter than original");
        assertTrue(longDescription.length() > 200, "Test description should be over 200 characters");
    }

    @Test
    void testParameterValidation() {
        // Given - mock service responses
        when(youTubeService.getLatestVideos(anyInt())).thenReturn(List.of(
            VideoInfo.basic("v1", "Test", "url", LocalDateTime.now(), 100L)
        ));

        // Test count parameter validation
        String result1 = youTubeTools.getLatestVideos("invalid");
        String result2 = youTubeTools.getLatestVideos("0");
        String result3 = youTubeTools.getLatestVideos("100");

        // Should use default values for invalid input
        assertTrue(result1.contains("Latest 1 videos")); // Uses default
        assertTrue(result2.contains("Latest 1 videos")); // Minimum 1
        assertTrue(result3.contains("Latest 1 videos")); // Maximum 50
    }

    @Test
    void testErrorHandling() {
        // Given - service throws exception
        when(youTubeService.getLatestVideos(anyInt()))
            .thenThrow(new RuntimeException("YouTube API error"));

        // When
        String result = youTubeTools.getLatestVideos("5");

        // Then
        assertTrue(result.startsWith("Error fetching latest videos:"));
        assertTrue(result.contains("YouTube API error"));
    }
}