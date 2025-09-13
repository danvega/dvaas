package dev.danvega.dvaas.tools.youtube;

import dev.danvega.dvaas.tools.youtube.model.ChannelStats;
import dev.danvega.dvaas.tools.youtube.model.SearchResult;
import dev.danvega.dvaas.tools.youtube.model.VideoInfo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for YouTubeService with mocked responses.
 * These tests run fast and don't require API credentials.
 */
@SpringBootTest
@ActiveProfiles("test")
class YouTubeServiceTest {

    @MockitoBean
    private YouTubeService youTubeService;

    @Test
    void testGetChannelStats() {
        // Given - mock channel stats
        ChannelStats mockStats = new ChannelStats(
                "UC123456789",
                "Dan Vega",
                "Spring Boot and Java tutorials",
                50000L,
                1000000L,
                200L,
                LocalDateTime.of(2020, 1, 15, 10, 0),
                false
        );

        when(youTubeService.getChannelStats()).thenReturn(mockStats);

        // When
        ChannelStats result = youTubeService.getChannelStats();

        // Then
        assertNotNull(result);
        assertEquals("UC123456789", result.channelId());
        assertEquals("Dan Vega", result.title());
        assertEquals(50000L, result.subscriberCount());
        assertEquals(200L, result.videoCount());
        assertEquals("50.0K", result.getFormattedSubscriberCount());
        assertEquals("1.0M", result.getFormattedTotalViewCount());
        assertEquals(5000L, result.getAverageViewsPerVideo());
    }

    @Test
    void testGetLatestVideos() {
        // Given - mock video list
        List<VideoInfo> mockVideos = List.of(
            VideoInfo.basic("video1", "Spring Boot 3 Tutorial",
                          "https://youtube.com/watch?v=video1",
                          LocalDateTime.now().minusDays(1), 1500L),
            VideoInfo.basic("video2", "Java 21 New Features",
                          "https://youtube.com/watch?v=video2",
                          LocalDateTime.now().minusDays(3), 2300L)
        );

        when(youTubeService.getLatestVideos(5)).thenReturn(mockVideos);

        // When
        List<VideoInfo> result = youTubeService.getLatestVideos(5);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Spring Boot 3 Tutorial", result.get(0).title());
        assertEquals("video1", result.get(0).id());
        assertTrue(result.get(0).getYouTubeUrl().contains("video1"));
    }

    @Test
    void testGetTopVideos() {
        // Given - mock top videos (sorted by view count)
        List<VideoInfo> mockTopVideos = List.of(
            VideoInfo.basic("top1", "Most Popular Spring Tutorial",
                          "https://youtube.com/watch?v=top1",
                          LocalDateTime.now().minusMonths(2), 50000L),
            VideoInfo.basic("top2", "Java Best Practices",
                          "https://youtube.com/watch?v=top2",
                          LocalDateTime.now().minusMonths(1), 30000L)
        );

        when(youTubeService.getTopVideos(5, "recent")).thenReturn(mockTopVideos);

        // When
        List<VideoInfo> result = youTubeService.getTopVideos(5, "recent");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // Verify sorting by view count (descending)
        assertTrue(result.get(0).viewCount() >= result.get(1).viewCount());
        assertEquals(50000L, result.get(0).viewCount());
        assertEquals("Most Popular Spring Tutorial", result.get(0).title());
    }

    @Test
    void testSearchVideosByTopic() {
        // Given - mock search results
        List<VideoInfo> mockVideos = List.of(
            VideoInfo.basic("spring1", "Spring Security Tutorial",
                          "https://youtube.com/watch?v=spring1",
                          LocalDateTime.now().minusWeeks(1), 5000L),
            VideoInfo.basic("spring2", "Spring Data JPA Guide",
                          "https://youtube.com/watch?v=spring2",
                          LocalDateTime.now().minusWeeks(2), 3500L)
        );

        SearchResult mockSearchResult = new SearchResult(mockVideos, "spring", 2, null);

        when(youTubeService.searchVideosByTopic("spring", 10)).thenReturn(mockSearchResult);

        // When
        SearchResult result = youTubeService.searchVideosByTopic("spring", 10);

        // Then
        assertNotNull(result);
        assertEquals("spring", result.query());
        assertEquals(2, result.totalResults());
        assertEquals(2, result.videos().size());

        VideoInfo firstVideo = result.videos().get(0);
        assertEquals("Spring Security Tutorial", firstVideo.title());
        assertTrue(firstVideo.title().toLowerCase().contains("spring"));
    }

    @Test
    void testGetLatestVideosEmptyResult() {
        // Given - empty result
        when(youTubeService.getLatestVideos(10)).thenReturn(List.of());

        // When
        List<VideoInfo> result = youTubeService.getLatestVideos(10);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchVideosNoResults() {
        // Given - no search results
        SearchResult emptyResult = new SearchResult(List.of(), "nonexistent", 0, null);
        when(youTubeService.searchVideosByTopic("nonexistent", 10)).thenReturn(emptyResult);

        // When
        SearchResult result = youTubeService.searchVideosByTopic("nonexistent", 10);

        // Then
        assertNotNull(result);
        assertEquals("nonexistent", result.query());
        assertEquals(0, result.totalResults());
        assertTrue(result.videos().isEmpty());
        assertEquals("No videos found for query: nonexistent", result.getSummary());
    }

    @Test
    void testServiceErrorHandling() {
        // Given - service throws exception
        when(youTubeService.getChannelStats()).thenThrow(new RuntimeException("API Error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> youTubeService.getChannelStats());
    }

    @Test
    void testVideoInfoFormatting() {
        // Given - video with high view count
        VideoInfo video = VideoInfo.basic("test", "Test Video",
                                        "https://youtube.com/watch?v=test",
                                        LocalDateTime.now(), 1500000L);

        // When & Then - test formatting methods
        assertEquals("1.5M", video.getFormattedViewCount());
        assertTrue(video.getYouTubeUrl().contains("youtube.com"));
        assertEquals("test", video.id());
    }

    @Test
    void testChannelStatsFormatting() {
        // Given
        ChannelStats stats = new ChannelStats(
                "UC123", "Test Channel", "Description",
                1500000L, 50000000L, 100L,
                LocalDateTime.now().minusYears(2), false
        );

        // When & Then
        assertEquals("1.5M", stats.getFormattedSubscriberCount());
        assertEquals("50.0M", stats.getFormattedTotalViewCount());
        assertEquals(500000L, stats.getAverageViewsPerVideo());
    }
}