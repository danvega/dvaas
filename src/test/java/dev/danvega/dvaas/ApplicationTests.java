package dev.danvega.dvaas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import dev.danvega.dvaas.tools.youtube.YouTubeService;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationTests {

	@MockitoBean
	private YouTubeService youTubeService;

	@Test
	void contextLoads() {
		// Context loading test - YouTube service is mocked to prevent API calls
	}

}
