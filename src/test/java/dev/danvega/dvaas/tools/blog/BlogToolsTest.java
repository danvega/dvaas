package dev.danvega.dvaas.tools.blog;

import dev.danvega.dvaas.tools.blog.model.BlogPost;
import dev.danvega.dvaas.tools.blog.model.BlogSearchResult;
import dev.danvega.dvaas.tools.blog.model.BlogStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BlogTools MCP class
 */
@ExtendWith(MockitoExtension.class)
class BlogToolsTest {

    @Mock
    private BlogService blogService;

    private BlogTools blogTools;

    @BeforeEach
    void setUp() {
        blogTools = new BlogTools(blogService);
    }

    @Test
    void getLatestPosts_WithDefaultCount_ShouldReturnFormattedResponse() {
        // Arrange
        List<BlogPost> mockPosts = List.of(
            new BlogPost(
                "Spring Boot 3.2 Features", "/blog/spring-boot-32", "guid1",
                "Exploring the new features in Spring Boot 3.2",
                LocalDateTime.of(2024, 1, 15, 10, 0),
                "Dan Vega", List.of("spring", "boot"), null
            ),
            new BlogPost(
                "AI with Spring Boot", "/blog/ai-spring-boot", "guid2",
                "Building AI applications with Spring Boot",
                LocalDateTime.of(2024, 1, 10, 14, 30),
                "Dan Vega", List.of("spring", "ai"), "https://youtube.com/watch?v=abc123"
            )
        );

        when(blogService.getLatestPosts(10)).thenReturn(mockPosts);

        // Act
        String result = blogTools.getLatestPosts(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Latest 2 blog posts"));
        assertTrue(result.contains("Spring Boot 3.2 Features"));
        assertTrue(result.contains("AI with Spring Boot"));
        assertTrue(result.contains("Jan 15, 2024"));
        assertTrue(result.contains("Jan 10, 2024"));
        assertTrue(result.contains("https://www.danvega.dev/blog/spring-boot-32"));
        assertTrue(result.contains("https://www.danvega.dev/blog/ai-spring-boot"));
        assertTrue(result.contains("🎥 YouTube: https://youtube.com/watch?v=abc123"));
        assertTrue(result.contains("🏷️ Tags: spring, boot"));
        verify(blogService).getLatestPosts(10);
    }

    @Test
    void getLatestPosts_WithCustomCount_ShouldRespectCount() {
        // Arrange
        when(blogService.getLatestPosts(5)).thenReturn(List.of());

        // Act
        String result = blogTools.getLatestPosts("5");

        // Assert
        assertEquals("No recent blog posts found.", result);
        verify(blogService).getLatestPosts(5);
    }

    @Test
    void getLatestPosts_WithInvalidCount_ShouldUseDefault() {
        // Arrange
        when(blogService.getLatestPosts(10)).thenReturn(List.of());

        // Act
        String result = blogTools.getLatestPosts("invalid");

        // Assert
        assertEquals("No recent blog posts found.", result);
        verify(blogService).getLatestPosts(10);
    }

    @Test
    void getLatestPosts_WhenServiceThrowsException_ShouldReturnErrorMessage() {
        // Arrange
        when(blogService.getLatestPosts(anyInt())).thenThrow(new RuntimeException("RSS feed error"));

        // Act
        String result = blogTools.getLatestPosts(null);

        // Assert
        assertTrue(result.startsWith("Error fetching latest blog posts:"));
        assertTrue(result.contains("RSS feed error"));
    }

    @Test
    void searchPostsByKeyword_WithValidKeyword_ShouldReturnFormattedResponse() {
        // Arrange
        List<BlogPost> mockPosts = List.of(
            new BlogPost(
                "Spring Security Tutorial", "/blog/spring-security", "guid1",
                "Complete guide to Spring Security",
                LocalDateTime.of(2024, 1, 20, 9, 0),
                "Dan Vega", List.of("spring", "security"), null
            )
        );

        BlogSearchResult searchResult = BlogSearchResult.forKeyword(mockPosts, "spring");
        when(blogService.searchPostsByKeyword("spring", 10)).thenReturn(searchResult);

        // Act
        String result = blogTools.searchPostsByKeyword("spring", null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Found 1 blog posts matching 'spring'"));
        assertTrue(result.contains("Spring Security Tutorial"));
        assertTrue(result.contains("Jan 20, 2024"));
        verify(blogService).searchPostsByKeyword("spring", 10);
    }

    @Test
    void searchPostsByKeyword_WithNullKeyword_ShouldReturnError() {
        // Act
        String result = blogTools.searchPostsByKeyword(null, null);

        // Assert
        assertEquals("Error: Keyword parameter is required.", result);
        verifyNoInteractions(blogService);
    }

    @Test
    void searchPostsByKeyword_WithEmptyKeyword_ShouldReturnError() {
        // Act
        String result = blogTools.searchPostsByKeyword("", null);

        // Assert
        assertEquals("Error: Keyword parameter is required.", result);
        verifyNoInteractions(blogService);
    }

    @Test
    void searchPostsByKeyword_WithNoResults_ShouldReturnNoResultsMessage() {
        // Arrange
        BlogSearchResult emptyResult = BlogSearchResult.forKeyword(List.of(), "nonexistent");
        when(blogService.searchPostsByKeyword("nonexistent", 10)).thenReturn(emptyResult);

        // Act
        String result = blogTools.searchPostsByKeyword("nonexistent", null);

        // Assert
        assertEquals("No blog posts found for keyword: 'nonexistent'", result);
        verify(blogService).searchPostsByKeyword("nonexistent", 10);
    }

    @Test
    void getPostsByDateRange_WithYear_ShouldReturnFormattedResponse() {
        // Arrange
        List<BlogPost> mockPosts = List.of(
            new BlogPost(
                "2024 Predictions", "/blog/2024-predictions", "guid1",
                "My predictions for 2024",
                LocalDateTime.of(2024, 1, 1, 12, 0),
                "Dan Vega", List.of(), null
            )
        );

        BlogSearchResult searchResult = BlogSearchResult.forDateRange(mockPosts, "2024");
        when(blogService.getPostsByYear(2024, 10)).thenReturn(searchResult);

        // Act
        String result = blogTools.getPostsByDateRange("2024", null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Found 1 blog posts in 2024"));
        assertTrue(result.contains("2024 Predictions"));
        verify(blogService).getPostsByYear(2024, 10);
    }

    @Test
    void getPostsByDateRange_WithDateRange_ShouldParseAndSearch() {
        // Arrange
        List<BlogPost> mockPosts = List.of();
        BlogSearchResult searchResult = BlogSearchResult.forDateRange(mockPosts, "2023-01-01 to 2023-12-31");
        when(blogService.getPostsByDateRange(any(), any(), eq(10))).thenReturn(searchResult);

        // Act
        String result = blogTools.getPostsByDateRange("2023-01-01 to 2023-12-31", null);

        // Assert
        assertTrue(result.contains("No blog posts found for date range"));
        verify(blogService).getPostsByDateRange(any(), any(), eq(10));
    }

    @Test
    void getPostsByDateRange_WithInvalidFormat_ShouldReturnError() {
        // Act
        String result = blogTools.getPostsByDateRange("invalid-format", null);

        // Assert
        assertTrue(result.startsWith("Error fetching posts by date range:"));
        assertTrue(result.contains("Invalid date range format"));
    }

    @Test
    void getPostsByDateRange_WithNullDateRange_ShouldReturnError() {
        // Act
        String result = blogTools.getPostsByDateRange(null, null);

        // Assert
        assertTrue(result.startsWith("Error: Date range parameter is required"));
    }

    @Test
    void getBlogStats_WithValidStats_ShouldReturnFormattedResponse() {
        // Arrange
        BlogStats mockStats = new BlogStats(
            150,
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.of(2024, 12, 31, 0, 0),
            25,
            5,
            2.5,
            30,
            "spring"
        );

        when(blogService.getBlogStats()).thenReturn(mockStats);

        // Act
        String result = blogTools.getBlogStats();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Dan Vega's Blog Statistics"));
        assertTrue(result.contains("📝 Total Posts: 150"));
        assertTrue(result.contains("📅 Blog Timespan: 4 years (2020 - 2024)"));
        assertTrue(result.contains("📊 Posting Frequency: Active (2-4 posts/month)"));
        assertTrue(result.contains("📈 Average Posts/Month: 2.5"));
        assertTrue(result.contains("🗓️ Posts This Year: 25"));
        assertTrue(result.contains("📆 Posts This Month: 5"));
        assertTrue(result.contains("🎥 Posts with YouTube Videos: 30 (20.0%)"));
        assertTrue(result.contains("🏷️ Most Common Tag: spring"));
        assertTrue(result.contains("📅 First Post: Jan 01, 2020"));
        assertTrue(result.contains("📅 Latest Post: Dec 31, 2024"));
        verify(blogService).getBlogStats();
    }

    @Test
    void getBlogStats_WithNoPosts_ShouldReturnNoStatsMessage() {
        // Arrange
        BlogStats emptyStats = new BlogStats(0, null, null, 0, 0, 0.0, 0, null);
        when(blogService.getBlogStats()).thenReturn(emptyStats);

        // Act
        String result = blogTools.getBlogStats();

        // Assert
        assertEquals("No blog statistics available - no posts found.", result);
        verify(blogService).getBlogStats();
    }

    @Test
    void getBlogStats_WhenServiceThrowsException_ShouldReturnErrorMessage() {
        // Arrange
        when(blogService.getBlogStats()).thenThrow(new RuntimeException("Stats calculation error"));

        // Act
        String result = blogTools.getBlogStats();

        // Assert
        assertTrue(result.startsWith("Error fetching blog statistics:"));
        assertTrue(result.contains("Stats calculation error"));
    }

    @Test
    void parseCount_WithValidNumbers_ShouldReturnParsedValue() {
        // Test via getLatestPosts which uses parseCount internally
        when(blogService.getLatestPosts(25)).thenReturn(List.of());

        String result = blogTools.getLatestPosts("25");

        verify(blogService).getLatestPosts(25);
    }

    @Test
    void parseCount_WithExceedsMax_ShouldCapAtMaxValue() {
        // Test via getLatestPosts which caps at 50
        when(blogService.getLatestPosts(50)).thenReturn(List.of());

        String result = blogTools.getLatestPosts("100");

        verify(blogService).getLatestPosts(50);
    }

    @Test
    void parseCount_WithNegativeValue_ShouldUseMinimum() {
        // Test via getLatestPosts which has minimum of 1
        when(blogService.getLatestPosts(1)).thenReturn(List.of());

        String result = blogTools.getLatestPosts("-5");

        verify(blogService).getLatestPosts(1);
    }
}