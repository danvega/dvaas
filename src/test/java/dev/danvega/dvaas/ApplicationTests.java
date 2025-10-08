package dev.danvega.dvaas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import dev.danvega.dvaas.tools.youtube.YouTubeService;
import dev.danvega.dvaas.tools.blog.BlogService;
import dev.danvega.dvaas.tools.speaking.SpeakingService;
import dev.danvega.dvaas.tools.newsletter.NewsletterService;
import dev.danvega.dvaas.tools.podcast.PodcastService;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationTests {

	@MockitoBean
	private YouTubeService youTubeService;

	@MockitoBean
	private BlogService blogService;

	@MockitoBean
	private SpeakingService speakingService;

	@MockitoBean
	private NewsletterService newsletterService;

	@MockitoBean
	private PodcastService podcastService;

	@Test
	void contextLoads() {
		// Context loading test - all services are mocked to prevent API calls
	}

}
