package dev.danvega.dvaas.prompts;

import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * MCP prompts for content reporting across all content types
 * (YouTube videos, blog posts, newsletter posts, podcast episodes)
 */
@Component
public class ContentPrompts {

    @McpPrompt(
        name = "content-report",
        description = "Generate a comprehensive content report across all content types (video, live stream, blog, newsletter, podcast) for a specified time period. Outputs CSV format with metrics."
    )
    public GetPromptResult contentReport(
            @McpArg(name = "year", description = "Year for the report (e.g., '2024', '2025'). Defaults to current year if not specified.", required = false)
            String year,
            @McpArg(name = "startDate", description = "Custom start date in YYYY-MM-DD format (e.g., '2024-01-01'). Overrides year if provided.", required = false)
            String startDate,
            @McpArg(name = "endDate", description = "Custom end date in YYYY-MM-DD format (e.g., '2024-12-31'). Overrides year if provided.", required = false)
            String endDate,
            @McpArg(name = "contentTypes", description = "Comma-separated list of content types to include: 'video', 'live stream', 'blog', 'newsletter', 'podcast', or 'all' for everything. Defaults to 'all'.", required = false)
            String contentTypes) {

        // Determine date range
        String dateRangeStr;
        if (startDate != null && endDate != null) {
            dateRangeStr = startDate + " to " + endDate;
        } else if (year != null) {
            dateRangeStr = year;
        } else {
            int currentYear = LocalDate.now().getYear();
            dateRangeStr = String.valueOf(currentYear);
        }

        // Determine content types filter
        String typesFilter = contentTypes != null && !contentTypes.trim().isEmpty()
            ? contentTypes.trim()
            : "all";

        // Build comprehensive instruction message
        StringBuilder instruction = new StringBuilder();
        instruction.append("Generate a comprehensive content report for the date range: ").append(dateRangeStr).append("\n\n");

        instruction.append("## Instructions:\n\n");

        instruction.append("### 1. Data Collection\n");
        instruction.append("Gather content from the following sources based on the content types filter ('").append(typesFilter).append("'):\n\n");

        if (shouldIncludeType(typesFilter, "video", "live stream")) {
            instruction.append("**YouTube Videos:**\n");
            instruction.append("- Use tool: `youtube-get-latest-videos` with a high count (e.g., 50)\n");
            instruction.append("- Then use tool: `youtube-search-videos-by-topic` to find additional videos if needed\n");
            instruction.append("- Filter results to the date range: ").append(dateRangeStr).append("\n");
            instruction.append("- Determine if each video is a 'video' or 'live stream' (if duration or title indicates live streaming)\n\n");
        }

        if (shouldIncludeType(typesFilter, "blog")) {
            instruction.append("**Blog Posts:**\n");
            instruction.append("- Use tool: `blog-get-posts-by-date-range` with dateRange parameter: '").append(dateRangeStr).append("'\n");
            instruction.append("- Retrieve up to 50 posts\n\n");
        }

        if (shouldIncludeType(typesFilter, "newsletter")) {
            instruction.append("**Newsletter Posts:**\n");
            instruction.append("- Use tool: `newsletter-get-latest-posts` with publication='all' and count=50\n");
            instruction.append("- Filter results to the date range: ").append(dateRangeStr).append("\n");
            instruction.append("- Only include posts with status 'confirmed' (published)\n\n");
        }

        if (shouldIncludeType(typesFilter, "podcast")) {
            instruction.append("**Podcast Episodes:**\n");
            instruction.append("- Use tool: `podcast-get-latest-episodes` with count=50\n");
            instruction.append("- Filter results to the date range: ").append(dateRangeStr).append("\n\n");
        }

        instruction.append("### 2. Data Formatting\n");
        instruction.append("Format the output as CSV with the following columns:\n\n");
        instruction.append("```\n");
        instruction.append("NAME,CONTENT TYPE,EXPORT INDICATOR,DATE,CONTENT LINK,EYEBALLS LIVE,EYEBALLS POST\n");
        instruction.append("```\n\n");

        instruction.append("**Column Specifications:**\n");
        instruction.append("- **NAME**: Title of the content\n");
        instruction.append("- **CONTENT TYPE**: One of: 'video', 'live stream', 'blog', 'newsletter', 'podcast'\n");
        instruction.append("- **EXPORT INDICATOR**: Always use 'export for reporting'\n");
        instruction.append("- **DATE**: Publication date in M/D/YYYY format (e.g., 11/5/2024)\n");
        instruction.append("- **CONTENT LINK**: Full URL to the content\n");
        instruction.append("- **EYEBALLS LIVE**: Always 0 (live viewer metrics not currently tracked)\n");
        instruction.append("- **EYEBALLS POST**: \n");
        instruction.append("  - For YouTube videos: Use the viewCount field\n");
        instruction.append("  - For all other content types: Use 0 (metrics not available via current APIs)\n\n");

        instruction.append("### 3. Sorting and Output\n");
        instruction.append("- Sort all content by date in chronological order (oldest to newest)\n");
        instruction.append("- Output the CSV with proper escaping for commas and quotes in titles\n");
        instruction.append("- Include the header row\n\n");

        instruction.append("### 4. Example Output Format\n");
        instruction.append("```csv\n");
        instruction.append("NAME,CONTENT TYPE,EXPORT INDICATOR,DATE,CONTENT LINK,EYEBALLS LIVE,EYEBALLS POST\n");
        instruction.append("Spring Security 6.4 - Rest Client OAuth2 Support,video,export for reporting,11/5/2024,https://youtu.be/nFKcJDpUuZ8,0,4000\n");
        instruction.append("Spring Data - Query by Example,video,export for reporting,11/8/2024,https://youtu.be/NGVWHdGNbiI,0,5000\n");
        instruction.append("Spring Boot Tips and Tricks,blog,export for reporting,11/15/2024,https://www.danvega.dev/blog/spring-boot-tips,0,0\n");
        instruction.append("Weekly Newsletter #45,newsletter,export for reporting,11/20/2024,https://www.danvega.dev/newsletter/45,0,0\n");
        instruction.append("Spring Office Hours Episode 50,podcast,export for reporting,11/25/2024,https://www.springofficehours.io/episodes/50,0,0\n");
        instruction.append("```\n\n");

        instruction.append("### 5. Important Notes\n");
        instruction.append("- If a tool returns an error, skip that content type and note it in a comment above the CSV\n");
        instruction.append("- Handle pagination if needed to get all content in the date range\n");
        instruction.append("- Ensure all dates are parsed correctly and fall within the specified range\n");
        instruction.append("- Remove any duplicate entries (same content appearing multiple times)\n");

        return new GetPromptResult(
            "Content Report Generation Instructions for " + dateRangeStr,
            List.of(new PromptMessage(Role.USER, new TextContent(instruction.toString())))
        );
    }

    /**
     * Helper method to determine if a content type should be included based on the filter
     */
    private boolean shouldIncludeType(String filter, String... types) {
        if ("all".equalsIgnoreCase(filter)) {
            return true;
        }

        String lowerFilter = filter.toLowerCase();
        for (String type : types) {
            if (lowerFilter.contains(type.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
