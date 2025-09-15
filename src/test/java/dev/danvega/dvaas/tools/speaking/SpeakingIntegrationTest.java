package dev.danvega.dvaas.tools.speaking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test for the complete speaking functionality
 * This test validates the Spring Boot integration and configuration
 */
@SpringBootTest
@TestPropertySource(properties = {
    "dvaas.speaking.api-url=https://www.danvega.dev/api/speaking",
    "dvaas.speaking.cache-duration=PT5M",
    // Ensure other services are configured for complete integration
    "dvaas.blog.rss-url=https://www.danvega.dev/rss.xml",
    "dvaas.blog.cache-duration=PT30M",
    "dvaas.youtube.api-key=test-key",
    "dvaas.youtube.channel-id=UCtest-channel-id-example",
    "dvaas.youtube.application-name=test-app"
})
class SpeakingIntegrationTest {

    @Test
    void contextLoads() {
        // This test ensures that the Spring context loads successfully
        // with all the speaking-related beans properly configured
        // The @SpringBootTest annotation will fail if there are any
        // configuration issues or missing dependencies
    }

    @Test
    void speakingConfigurationIsValid() {
        // This test validates that all the configuration properties
        // are properly validated and the beans can be created
        // The TestPropertySource ensures we have valid test configuration
        // that follows the same validation rules as production
    }
}