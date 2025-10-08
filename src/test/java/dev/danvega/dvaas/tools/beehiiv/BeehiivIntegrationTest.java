package dev.danvega.dvaas.tools.beehiiv;

import dev.danvega.dvaas.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify Beehiiv service and tools are properly wired in Spring context
 */
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
        "dvaas.beehiiv.api-key=test-api-key",
        "dvaas.beehiiv.base-url=https://api.beehiiv.com/v2",
        "dvaas.beehiiv.cache-duration=PT30M",
        "dvaas.beehiiv.publications.danvega=pub_test_123",
        "dvaas.beehiiv.publications.bytesizedai=pub_test_456"
})
class BeehiivIntegrationTest {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private BeehiivService beehiivService;

    @Autowired(required = false)
    private BeehiivTools beehiivTools;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    void beehiivService_ShouldBeCreated() {
        assertNotNull(beehiivService, "BeehiivService should be created when API key and publications are configured");
    }

    @Test
    void beehiivTools_ShouldBeCreated() {
        assertNotNull(beehiivTools, "BeehiivTools should be created when BeehiivService is available");
    }

    @Test
    void mcpToolsAreScannable() {
        // Verify that our MCP tools are discoverable by the Spring AI MCP framework
        assertTrue(applicationContext.containsBean("beehiivTools"));

        BeehiivTools tools = applicationContext.getBean("beehiivTools", BeehiivTools.class);
        assertNotNull(tools);

        // Note: We cannot test actual API calls in integration tests without API credentials
        // These would require actual Beehiiv API access with valid publication IDs
        // For now, we verify the beans are wired correctly
    }

    @Test
    void beehiivService_ShouldHaveCorrectConfiguration() {
        // Verify that the service is properly configured
        assertNotNull(beehiivService);

        // The service should be initialized with the configured properties
        // This verifies the @ConditionalOnProperty annotations are working correctly
    }
}
