package dev.danvega.dvaas.tools.beehiiv;

import dev.danvega.dvaas.config.BeehiivProperties;
import dev.danvega.dvaas.tools.beehiiv.model.Post;
import dev.danvega.dvaas.tools.beehiiv.model.PostStats;
import dev.danvega.dvaas.tools.beehiiv.model.PublicationStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeehiivServiceTest {

    private BeehiivProperties beehiivProperties;
    private List<Post> testPosts;

    @BeforeEach
    void setUp() {
        // Create test properties
        Map<String, String> publications = Map.of(
                "danvega", "pub_123",
                "bytesizedai", "pub_456"
        );

        beehiivProperties = new BeehiivProperties(
                "test-api-key",
                "https://api.beehiiv.com/v2",
                Duration.ofMinutes(30),
                publications
        );

        // Create test posts
        testPosts = List.of(
                new Post(
                        "post_1",
                        "pub_123",
                        "danvega",
                        "Spring Boot 3.5 Release",
                        List.of("Dan Vega"),
                        "confirmed",
                        LocalDateTime.of(2024, 12, 1, 10, 0),
                        LocalDateTime.of(2024, 12, 1, 10, 0),
                        "https://danvega.dev/newsletter/spring-boot-35",
                        "https://example.com/thumb1.jpg",
                        "Spring Boot 3.5 has been released with exciting new features...",
                        "both",
                        "free",
                        List.of("spring", "java"),
                        new PostStats(1000, 50, 800, 40)
                ),
                new Post(
                        "post_2",
                        "pub_456",
                        "bytesizedai",
                        "AI Weekly Update",
                        List.of("Dan Vega"),
                        "confirmed",
                        LocalDateTime.of(2024, 11, 28, 9, 0),
                        LocalDateTime.of(2024, 11, 28, 9, 0),
                        "https://bytesizedai.dev/newsletter/ai-weekly",
                        "https://example.com/thumb2.jpg",
                        "Latest AI developments and news from the week...",
                        "both",
                        "free",
                        List.of("ai", "machine-learning"),
                        new PostStats(500, 25, 400, 20)
                ),
                new Post(
                        "post_3",
                        "pub_123",
                        "danvega",
                        "Java 24 Preview Features",
                        List.of("Dan Vega"),
                        "draft",
                        null,
                        null,
                        null,
                        null,
                        "Exploring the new preview features in Java 24...",
                        "web",
                        "free",
                        List.of("java"),
                        null
                )
        );
    }

    @Test
    void testBeehiivPropertiesConfiguration() {
        assertThat(beehiivProperties.apiKey()).isEqualTo("test-api-key");
        assertThat(beehiivProperties.baseUrl()).isEqualTo("https://api.beehiiv.com/v2");
        assertThat(beehiivProperties.getCacheDurationMinutes()).isEqualTo(30L);
        assertThat(beehiivProperties.isEnabled()).isTrue();
        assertThat(beehiivProperties.getPublicationNames()).containsExactlyInAnyOrder("danvega", "bytesizedai");
    }

    @Test
    void testBeehiivPropertiesPublicationQueries() {
        assertThat(beehiivProperties.hasPublication("danvega")).isTrue();
        assertThat(beehiivProperties.hasPublication("bytesizedai")).isTrue();
        assertThat(beehiivProperties.hasPublication("unknown")).isFalse();
        assertThat(beehiivProperties.getPublicationId("danvega")).isEqualTo("pub_123");
        assertThat(beehiivProperties.getPublicationId("bytesizedai")).isEqualTo("pub_456");
    }

    @Test
    void testBeehiivPropertiesValidation() {
        // Test cache duration validation
        assertThatThrownBy(() -> new BeehiivProperties(
                "api-key",
                "https://api.beehiiv.com/v2",
                Duration.ofSeconds(30), // Less than 1 minute
                Map.of("danvega", "pub_123")
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("at least 1 minute");

        // Test empty publications map
        assertThatThrownBy(() -> new BeehiivProperties(
                "api-key",
                "https://api.beehiiv.com/v2",
                Duration.ofMinutes(30),
                Map.of() // Empty map
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("at least one publication");

        // Test blank publication ID
        assertThatThrownBy(() -> new BeehiivProperties(
                "api-key",
                "https://api.beehiiv.com/v2",
                Duration.ofMinutes(30),
                Map.of("danvega", "") // Blank ID
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("must not be blank");
    }

    @Test
    void testPostModel() {
        Post post = testPosts.get(0);

        assertThat(post.id()).isEqualTo("post_1");
        assertThat(post.publicationName()).isEqualTo("danvega");
        assertThat(post.title()).isEqualTo("Spring Boot 3.5 Release");
        assertThat(post.isPublished()).isTrue();
        assertThat(post.isDraft()).isFalse();
        assertThat(post.isArchived()).isFalse();
        assertThat(post.getAuthorsFormatted()).isEqualTo("Dan Vega");
        assertThat(post.getEffectivePublishDate()).isEqualTo(LocalDateTime.of(2024, 12, 1, 10, 0));
    }

    @Test
    void testPostModelDraft() {
        Post draftPost = testPosts.get(2);

        assertThat(draftPost.isDraft()).isTrue();
        assertThat(draftPost.isPublished()).isFalse();
    }

    @Test
    void testPostStats() {
        PostStats stats = testPosts.get(0).stats();

        assertThat(stats).isNotNull();
        assertThat(stats.opens()).isEqualTo(1000);
        assertThat(stats.clicks()).isEqualTo(50);
        assertThat(stats.uniqueOpens()).isEqualTo(800);
        assertThat(stats.uniqueClicks()).isEqualTo(40);
        assertThat(stats.hasStats()).isTrue();

        // Test rate calculations
        assertThat(stats.getOpenRate(1000)).isEqualTo(80.0);
        assertThat(stats.getClickRate(1000)).isEqualTo(4.0);
        assertThat(stats.getClickToOpenRate()).isEqualTo(5.0);
    }

    @Test
    void testPostStatsEmpty() {
        PostStats emptyStats = PostStats.empty();

        assertThat(emptyStats.hasStats()).isFalse();
        assertThat(emptyStats.opens()).isEqualTo(0);
        assertThat(emptyStats.clicks()).isEqualTo(0);
    }

    @Test
    void testPublicationStatsBasic() {
        PublicationStats stats = PublicationStats.basic(
                "pub_123",
                "danvega",
                50,
                LocalDateTime.of(2020, 1, 1, 0, 0)
        );

        assertThat(stats.publicationId()).isEqualTo("pub_123");
        assertThat(stats.name()).isEqualTo("danvega");
        assertThat(stats.totalPosts()).isEqualTo(50);
    }

    @Test
    void testPublicationStatsCalculations() {
        PublicationStats stats = new PublicationStats(
                "pub_123",
                "danvega",
                100,
                80,
                20,
                10000L,
                8000L,
                2000L,
                45.5,
                12.3,
                5000L,
                2275L,
                615L,
                LocalDateTime.of(2020, 1, 1, 0, 0)
        );

        assertThat(stats.getPublishedPercentage()).isEqualTo(80.0);
        assertThat(stats.getDraftPercentage()).isEqualTo(20.0);
        assertThat(stats.getPremiumSubscriberPercentage()).isEqualTo(20.0);
        assertThat(stats.getEngagementScore()).isEqualTo(28.9); // (45.5 + 12.3) / 2
    }
}
