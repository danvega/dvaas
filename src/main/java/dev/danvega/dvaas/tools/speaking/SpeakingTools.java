package dev.danvega.dvaas.tools.speaking;

import dev.danvega.dvaas.tools.speaking.model.SpeakingEngagement;
import dev.danvega.dvaas.tools.speaking.model.SpeakingSearchResult;
import dev.danvega.dvaas.tools.speaking.model.SpeakingStats;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * MCP tools for speaking engagement operations
 */
@Component
@ConditionalOnBean(SpeakingService.class)
public class SpeakingTools {

    private final SpeakingService speakingService;

    public SpeakingTools(SpeakingService speakingService) {
        this.speakingService = speakingService;
    }

    @McpTool(name = "speaking-get-latest-engagements",
             description = "Get the most recent speaking engagements from Dan Vega's speaking schedule")
    public String getLatestEngagements(
            @McpToolParam(description = "Number of engagements to retrieve (default: 10, max: 50)",
                         required = false) String count) {

        int maxResults = parseCount(count, 10, 50);

        try {
            List<SpeakingEngagement> engagements = speakingService.getLatestEngagements(maxResults);

            if (engagements.isEmpty()) {
                return "No recent speaking engagements found.";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Latest %d speaking engagements from Dan Vega:\n\n", engagements.size()));

            for (int i = 0; i < engagements.size(); i++) {
                SpeakingEngagement engagement = engagements.get(i);
                result.append(String.format("%d. **%s**\n", i + 1, engagement.title()));
                result.append(String.format("   🎪 Event: %s\n", engagement.name()));
                result.append(String.format("   📅 Date: %s\n", engagement.getFormattedDateRange()));
                result.append(String.format("   📍 Location: %s\n", engagement.location()));
                result.append(String.format("   🎯 Status: %s\n", engagement.getEventStatus()));

                if (engagement.hasUrl()) {
                    result.append(String.format("   🔗 URL: %s\n", engagement.getFullUrl()));
                }

                String shortDesc = engagement.getShortDescription();
                if (!shortDesc.isEmpty()) {
                    result.append(String.format("   📝 %s\n", shortDesc));
                }

                String topics = engagement.extractTopics();
                if (!topics.isEmpty()) {
                    result.append(String.format("   🏷️ Topics: %s\n", topics));
                }

                result.append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching latest speaking engagements: " + e.getMessage();
        }
    }

    @McpTool(name = "speaking-get-upcoming-events",
             description = "Get upcoming speaking events from Dan Vega's speaking schedule")
    public String getUpcomingEvents(
            @McpToolParam(description = "Number of events to retrieve (default: 10, max: 50)",
                         required = false) String count) {

        int maxResults = parseCount(count, 10, 50);

        try {
            List<SpeakingEngagement> upcomingEvents = speakingService.getUpcomingEngagements(maxResults);

            if (upcomingEvents.isEmpty()) {
                return "No upcoming speaking events found.";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Upcoming %d speaking events for Dan Vega:\n\n", upcomingEvents.size()));

            for (int i = 0; i < upcomingEvents.size(); i++) {
                SpeakingEngagement engagement = upcomingEvents.get(i);
                result.append(String.format("%d. **%s**\n", i + 1, engagement.title()));
                result.append(String.format("   🎪 Event: %s\n", engagement.name()));
                result.append(String.format("   📅 Date: %s\n", engagement.getFormattedDateRange()));
                result.append(String.format("   📍 Location: %s\n", engagement.location()));
                result.append(String.format("   🎭 Type: %s\n", engagement.getEventType()));

                if (engagement.hasUrl()) {
                    result.append(String.format("   🔗 URL: %s\n", engagement.getFullUrl()));
                }

                String shortDesc = engagement.getShortDescription();
                if (!shortDesc.isEmpty()) {
                    result.append(String.format("   📝 %s\n", shortDesc));
                }

                String topics = engagement.extractTopics();
                if (!topics.isEmpty()) {
                    result.append(String.format("   🏷️ Topics: %s\n", topics));
                }

                result.append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching upcoming speaking events: " + e.getMessage();
        }
    }

    @McpTool(name = "speaking-search-by-topic",
             description = "Search for speaking engagements by topic or keyword (e.g., 'spring', 'ai', 'java', 'microservices')")
    public String searchByTopic(
            @McpToolParam(description = "Topic or keyword to search for in titles, descriptions, or event names",
                         required = true) String topic,
            @McpToolParam(description = "Number of engagements to retrieve (default: 10, max: 50)",
                         required = false) String count) {

        if (topic == null || topic.trim().isEmpty()) {
            return "Error: Topic parameter is required.";
        }

        int maxResults = parseCount(count, 10, 50);

        try {
            SpeakingSearchResult searchResult = speakingService.searchEngagementsByKeyword(topic.trim(), maxResults);

            if (!searchResult.hasResults()) {
                return String.format("No speaking engagements found for topic: '%s'", topic);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Found %d speaking engagements about '%s':\n\n",
                searchResult.totalMatches(), topic));

            for (int i = 0; i < searchResult.engagements().size(); i++) {
                SpeakingEngagement engagement = searchResult.engagements().get(i);
                result.append(String.format("%d. **%s**\n", i + 1, engagement.title()));
                result.append(String.format("   🎪 Event: %s\n", engagement.name()));
                result.append(String.format("   📅 Date: %s\n", engagement.getFormattedDateRange()));
                result.append(String.format("   📍 Location: %s\n", engagement.location()));
                result.append(String.format("   🎯 Status: %s\n", engagement.getEventStatus()));

                if (engagement.hasUrl()) {
                    result.append(String.format("   🔗 URL: %s\n", engagement.getFullUrl()));
                }

                String shortDesc = engagement.getShortDescription();
                if (!shortDesc.isEmpty()) {
                    result.append(String.format("   📝 %s\n", shortDesc));
                }

                result.append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "Error searching speaking engagements: " + e.getMessage();
        }
    }

    @McpTool(name = "speaking-get-stats",
             description = "Get overall statistics and information about Dan Vega's speaking engagements")
    public String getSpeakingStats() {
        try {
            SpeakingStats stats = speakingService.getSpeakingStats();

            if (stats.totalEngagements() == 0) {
                return "No speaking engagement statistics available - no events found.";
            }

            StringBuilder result = new StringBuilder();
            result.append("**Dan Vega's Speaking Engagement Statistics**\n\n");
            result.append(String.format("🎤 Total Engagements: %,d\n", stats.totalEngagements()));
            result.append(String.format("📅 Speaking Timespan: %s\n", stats.getSpeakingTimespan()));
            result.append(String.format("📊 Speaking Frequency: %s\n", stats.getSpeakingFrequency()));
            result.append(String.format("📈 Average Engagements/Month: %.1f\n", stats.averageEventsPerMonth()));

            result.append(String.format("\n🔮 Upcoming Events: %,d\n", stats.upcomingEvents()));
            result.append(String.format("📚 Past Events: %,d\n", stats.pastEvents()));

            if (stats.nextEventDate() != null) {
                result.append(String.format("⏰ Next Event: %s\n", stats.getNextEventInfo()));
            }

            if (stats.mostCommonLocation() != null) {
                result.append(String.format("\n📍 Most Common Location: %s\n", stats.mostCommonLocation()));
            }

            if (stats.mostCommonEventType() != null) {
                result.append(String.format("🎭 Most Common Event Type: %s\n", stats.mostCommonEventType()));
            }

            String topLocations = stats.getTopLocations();
            if (!topLocations.equals("No location data available")) {
                result.append(String.format("🌍 Top Locations: %s\n", topLocations));
            }

            String topEventTypes = stats.getTopEventTypes();
            if (!topEventTypes.equals("No event type data available")) {
                result.append(String.format("🎪 Event Types: %s\n", topEventTypes));
            }

            String eventTypeDistribution = stats.getEventTypeDistribution();
            if (!eventTypeDistribution.equals("No event type distribution available")) {
                result.append(String.format("\n📊 Event Type Distribution: %s\n", eventTypeDistribution));
            }

            if (stats.firstEventDate() != null) {
                result.append(String.format("\n📅 First Speaking Event: %s\n",
                    stats.firstEventDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
            }

            result.append(String.format("\n🎯 Speaker Status: %s\n",
                stats.isActiveSpeaker() ? "Active Speaker" : "Less Active"));

            return result.toString();
        } catch (Exception e) {
            return "Error fetching speaking statistics: " + e.getMessage();
        }
    }

    /**
     * Parse date range input and perform search
     */
    private SpeakingSearchResult parseDateRangeAndSearch(String dateRange, int maxResults) {
        // Handle year-only format (e.g., "2024")
        if (dateRange.matches("\\d{4}")) {
            int year = Integer.parseInt(dateRange);
            return speakingService.getEngagementsByYear(year, maxResults);
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
                return speakingService.getEngagementsByDateRange(startDate, endDate, maxResults);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD format.");
            }
        }

        // Handle single date format (e.g., "2023-12-25")
        if (dateRange.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                LocalDateTime singleDate = LocalDateTime.parse(dateRange + "T00:00:00");
                LocalDateTime endOfDay = singleDate.plusDays(1).minusSeconds(1);
                return speakingService.getEngagementsByDateRange(singleDate, endOfDay, maxResults);
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