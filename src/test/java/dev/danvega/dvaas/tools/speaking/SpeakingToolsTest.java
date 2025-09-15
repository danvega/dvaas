package dev.danvega.dvaas.tools.speaking;

import dev.danvega.dvaas.tools.speaking.model.SpeakingEngagement;
import dev.danvega.dvaas.tools.speaking.model.SpeakingSearchResult;
import dev.danvega.dvaas.tools.speaking.model.SpeakingStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpeakingToolsTest {

    @Mock
    private SpeakingService speakingService;

    private SpeakingTools speakingTools;

    private List<SpeakingEngagement> testEngagements;
    private SpeakingStats testStats;

    @BeforeEach
    void setUp() {
        speakingTools = new SpeakingTools(speakingService);

        // Create test engagements
        testEngagements = List.of(
            new SpeakingEngagement(
                "Spring Boot 3.0 Best Practices",
                "https://springone.com/session1",
                "SpringOne",
                LocalDateTime.of(2024, 12, 15, 10, 0),
                LocalDateTime.of(2024, 12, 15, 11, 0),
                "San Francisco, CA",
                "Deep dive into Spring Boot 3.0 features and best practices for modern application development"
            ),
            new SpeakingEngagement(
                "AI-Powered Applications with Spring AI",
                "https://javazone.com/session2",
                "JavaZone",
                LocalDateTime.now().plusDays(30), // Future event
                LocalDateTime.now().plusDays(30).plusHours(1),
                "Oslo, Norway",
                "Building intelligent applications using Spring AI framework"
            )
        );

        // Create test stats
        testStats = new SpeakingStats(
            25,
            5,
            20,
            LocalDateTime.of(2020, 1, 1, 0, 0),
            LocalDateTime.now().plusDays(15),
            "Virtual",
            "Conference",
            Map.of("Virtual", 10, "San Francisco", 8, "New York", 7),
            Map.of("Conference", 20, "Meetup", 3, "Workshop", 2),
            2.1
        );
    }

    @Test
    void testGetLatestEngagements() {
        when(speakingService.getLatestEngagements(anyInt())).thenReturn(testEngagements);

        String result = speakingTools.getLatestEngagements("10");

        assertThat(result).contains("Latest 2 speaking engagements from Dan Vega");
        assertThat(result).contains("Spring Boot 3.0 Best Practices");
        assertThat(result).contains("SpringOne");
        assertThat(result).contains("San Francisco, CA");
        assertThat(result).contains("🎪 Event:");
        assertThat(result).contains("📅 Date:");
        assertThat(result).contains("📍 Location:");
        assertThat(result).contains("🎯 Status:");
        assertThat(result).contains("🔗 URL:");
    }

    @Test
    void testGetLatestEngagementsEmpty() {
        when(speakingService.getLatestEngagements(anyInt())).thenReturn(List.of());

        String result = speakingTools.getLatestEngagements("10");

        assertThat(result).isEqualTo("No recent speaking engagements found.");
    }

    @Test
    void testGetLatestEngagementsWithNullCount() {
        when(speakingService.getLatestEngagements(10)).thenReturn(testEngagements);

        String result = speakingTools.getLatestEngagements(null);

        assertThat(result).contains("Latest 2 speaking engagements from Dan Vega");
    }

    @Test
    void testGetUpcomingEvents() {
        List<SpeakingEngagement> upcomingEngagements = testEngagements.stream()
                .filter(SpeakingEngagement::isUpcoming)
                .toList();

        when(speakingService.getUpcomingEngagements(anyInt())).thenReturn(upcomingEngagements);

        String result = speakingTools.getUpcomingEvents("5");

        assertThat(result).contains("Upcoming");
        assertThat(result).contains("speaking events for Dan Vega");
        assertThat(result).contains("AI-Powered Applications with Spring AI");
        assertThat(result).contains("JavaZone");
        assertThat(result).contains("Oslo, Norway");
        assertThat(result).contains("🎪 Event:");
        assertThat(result).contains("📅 Date:");
        assertThat(result).contains("📍 Location:");
        assertThat(result).contains("🎭 Type:");
    }

    @Test
    void testGetUpcomingEventsEmpty() {
        when(speakingService.getUpcomingEngagements(anyInt())).thenReturn(List.of());

        String result = speakingTools.getUpcomingEvents("10");

        assertThat(result).isEqualTo("No upcoming speaking events found.");
    }

    @Test
    void testSearchByTopic() {
        SpeakingSearchResult searchResult = SpeakingSearchResult.forKeyword(testEngagements, "spring");
        when(speakingService.searchEngagementsByKeyword(anyString(), anyInt())).thenReturn(searchResult);

        String result = speakingTools.searchByTopic("spring", "10");

        assertThat(result).contains("Found 2 speaking engagements about 'spring'");
        assertThat(result).contains("Spring Boot 3.0 Best Practices");
        assertThat(result).contains("AI-Powered Applications with Spring AI");
        assertThat(result).contains("🎪 Event:");
        assertThat(result).contains("📅 Date:");
        assertThat(result).contains("📍 Location:");
        assertThat(result).contains("🎯 Status:");
    }

    @Test
    void testSearchByTopicEmpty() {
        SpeakingSearchResult emptyResult = SpeakingSearchResult.forKeyword(List.of(), "nonexistent");
        when(speakingService.searchEngagementsByKeyword(anyString(), anyInt())).thenReturn(emptyResult);

        String result = speakingTools.searchByTopic("nonexistent", "10");

        assertThat(result).isEqualTo("No speaking engagements found for topic: 'nonexistent'");
    }

    @Test
    void testSearchByTopicNullTopic() {
        String result = speakingTools.searchByTopic(null, "10");

        assertThat(result).isEqualTo("Error: Topic parameter is required.");
    }

    @Test
    void testSearchByTopicEmptyTopic() {
        String result = speakingTools.searchByTopic("  ", "10");

        assertThat(result).isEqualTo("Error: Topic parameter is required.");
    }

    @Test
    void testGetSpeakingStats() {
        when(speakingService.getSpeakingStats()).thenReturn(testStats);

        String result = speakingTools.getSpeakingStats();

        assertThat(result).contains("Dan Vega's Speaking Engagement Statistics");
        assertThat(result).contains("🎤 Total Engagements: 25");
        assertThat(result).contains("🔮 Upcoming Events: 5");
        assertThat(result).contains("📚 Past Events: 20");
        assertThat(result).contains("📊 Speaking Frequency:");
        assertThat(result).contains("📈 Average Engagements/Month: 2.1");
        assertThat(result).contains("📍 Most Common Location: Virtual");
        assertThat(result).contains("🎭 Most Common Event Type: Conference");
        assertThat(result).contains("🌍 Top Locations:");
        assertThat(result).contains("🎪 Event Types:");
        assertThat(result).contains("📊 Event Type Distribution:");
        assertThat(result).contains("🎯 Speaker Status: Active Speaker");
        assertThat(result).contains("⏰ Next Event:");
    }

    @Test
    void testGetSpeakingStatsEmpty() {
        SpeakingStats emptyStats = new SpeakingStats(0, 0, 0, null, null, null, null, Map.of(), Map.of(), 0.0);
        when(speakingService.getSpeakingStats()).thenReturn(emptyStats);

        String result = speakingTools.getSpeakingStats();

        assertThat(result).isEqualTo("No speaking engagement statistics available - no events found.");
    }

    @Test
    void testGetSpeakingStatsException() {
        when(speakingService.getSpeakingStats()).thenThrow(new RuntimeException("API error"));

        String result = speakingTools.getSpeakingStats();

        assertThat(result).isEqualTo("Error fetching speaking statistics: API error");
    }

    @Test
    void testParseCountValidation() {
        when(speakingService.getLatestEngagements(15)).thenReturn(testEngagements);
        when(speakingService.getLatestEngagements(50)).thenReturn(testEngagements);
        when(speakingService.getLatestEngagements(10)).thenReturn(testEngagements);

        // Test valid count
        String result1 = speakingTools.getLatestEngagements("15");
        assertThat(result1).contains("Latest 2 speaking engagements");

        // Test max count enforcement (should cap at 50)
        String result2 = speakingTools.getLatestEngagements("100");
        assertThat(result2).contains("Latest 2 speaking engagements");

        // Test invalid count (should use default)
        String result3 = speakingTools.getLatestEngagements("invalid");
        assertThat(result3).contains("Latest 2 speaking engagements");
    }

    @Test
    void testGetLatestEngagementsWithException() {
        when(speakingService.getLatestEngagements(anyInt())).thenThrow(new RuntimeException("Service error"));

        String result = speakingTools.getLatestEngagements("10");

        assertThat(result).isEqualTo("Error fetching latest speaking engagements: Service error");
    }

    @Test
    void testGetUpcomingEventsWithException() {
        when(speakingService.getUpcomingEngagements(anyInt())).thenThrow(new RuntimeException("Service error"));

        String result = speakingTools.getUpcomingEvents("10");

        assertThat(result).isEqualTo("Error fetching upcoming speaking events: Service error");
    }

    @Test
    void testSearchByTopicWithException() {
        when(speakingService.searchEngagementsByKeyword(anyString(), anyInt()))
                .thenThrow(new RuntimeException("Search error"));

        String result = speakingTools.searchByTopic("spring", "10");

        assertThat(result).isEqualTo("Error searching speaking engagements: Search error");
    }
}