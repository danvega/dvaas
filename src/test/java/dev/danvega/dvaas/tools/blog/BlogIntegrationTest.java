package dev.danvega.dvaas.tools.blog;

import dev.danvega.dvaas.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify blog service and tools are properly wired in Spring context
 */
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "dvaas.blog.rss-url=https://www.danvega.dev/rss.xml",
    "dvaas.blog.cache-duration=PT30M",
    "dvaas.youtube.api-key=test-api-key-12345678901234567890",
    "dvaas.youtube.channel-id=UCtest123456789012345678",
    "dvaas.youtube.application-name=dvaas-test-youtube"
})
class BlogIntegrationTest {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private BlogService blogService;

    @Autowired(required = false)
    private BlogTools blogTools;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    void blogService_ShouldBeCreated() {
        assertNotNull(blogService, "BlogService should be created when RSS URL is configured");
    }

    @Test
    void blogTools_ShouldBeCreated() {
        assertNotNull(blogTools, "BlogTools should be created when BlogService is available");
    }

    @Test
    void mcpToolsAreScannable() {
        // Verify that our MCP tools are discoverable by the Spring AI MCP framework
        assertTrue(applicationContext.containsBean("blogTools"));

        BlogTools tools = applicationContext.getBean("blogTools", BlogTools.class);
        assertNotNull(tools);

        // Verify the tools can be invoked (they should handle empty results gracefully)
        String latestResult = tools.getLatestPosts("1");
        assertNotNull(latestResult);

        String statsResult = tools.getBlogStats();
        assertNotNull(statsResult);

        String searchResult = tools.searchPostsByKeyword("spring", "1");
        assertNotNull(searchResult);

        String dateRangeResult = tools.getPostsByDateRange("2024", "1");
        assertNotNull(dateRangeResult);
    }
}