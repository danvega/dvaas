package dev.danvega.dvaas.tools.podcast;

import com.fasterxml.jackson.databind.JsonNode;
import dev.danvega.dvaas.config.PodcastProperties;
import dev.danvega.dvaas.tools.podcast.model.Episode;
import dev.danvega.dvaas.tools.podcast.model.PodcastStats;
import dev.danvega.dvaas.tools.podcast.model.Show;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@ConditionalOnProperty(name = "dvaas.podcast.api-key")
public class PodcastService {

    private static final Logger logger = LoggerFactory.getLogger(PodcastService.class);
    private static final String API_BASE_URL = "https://api.transistor.fm/v1";

    private final RestClient restClient;
    private final PodcastProperties podcastProperties;
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private LocalDateTime lastCacheTime;

    public PodcastService(PodcastProperties podcastProperties) {
        this.podcastProperties = podcastProperties;
        this.restClient = RestClient.builder()
                .baseUrl(API_BASE_URL)
                .defaultHeader("x-api-key", podcastProperties.apiKey())
                .build();

        logger.info("Podcast service initialized with cache duration: {} minutes", podcastProperties.getCacheDurationMinutes());
    }

    public List<Show> getAllShows() {
        return getCachedShows();
    }

    public Show getShowById(String showId) {
        try {
            JsonNode response = restClient.get()
                    .uri("/shows/{id}", showId)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("data")) {
                return parseShowFromJson(response.get("data"));
            }

            throw new RuntimeException("Show not found: " + showId);
        } catch (Exception e) {
            logger.error("Error fetching show by ID: {}", showId, e);
            throw new RuntimeException("Failed to fetch show: " + showId, e);
        }
    }

    public String resolveShowIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }

        // First, try to resolve as a known show name from configuration
        Optional<String> configuredShowId = podcastProperties.getShowIdByName(identifier);
        if (configuredShowId.isPresent()) {
            logger.debug("Resolved show name '{}' to ID: {}", identifier, configuredShowId.get());
            return configuredShowId.get();
        }

        // If not a known name, check if it's a valid show ID by looking it up in cached shows
        List<Show> allShows = getCachedShows();

        // Try exact ID match
        Optional<Show> matchById = allShows.stream()
                .filter(show -> show.id().equals(identifier))
                .findFirst();

        if (matchById.isPresent()) {
            return matchById.get().id();
        }

        // Try case-insensitive name match in all shows
        String normalizedIdentifier = identifier.toLowerCase().trim();
        Optional<Show> matchByName = allShows.stream()
                .filter(show -> show.title() != null &&
                               show.title().toLowerCase().contains(normalizedIdentifier))
                .findFirst();

        if (matchByName.isPresent()) {
            logger.debug("Resolved show name '{}' to ID: {}", identifier, matchByName.get().id());
            return matchByName.get().id();
        }

        // If we can't resolve it, return it as-is (might be a valid ID we just don't have cached)
        logger.warn("Could not resolve show identifier '{}', using as-is", identifier);
        return identifier;
    }

    public List<Episode> getLatestEpisodes(int maxResults, String showIdentifier) {
        List<Episode> allEpisodes = getCachedEpisodes();

        // Filter by show if identifier provided
        if (showIdentifier != null && !showIdentifier.trim().isEmpty()) {
            String resolvedShowId = resolveShowIdentifier(showIdentifier);
            allEpisodes = allEpisodes.stream()
                    .filter(episode -> resolvedShowId.equals(episode.showId()))
                    .toList();
        }

        return allEpisodes.stream()
                .filter(Episode::isPublished)
                .sorted((e1, e2) -> e2.publishedAt().compareTo(e1.publishedAt()))
                .limit(Math.min(maxResults, 50))
                .toList();
    }

    public List<Episode> searchEpisodes(String keyword, int maxResults, String showIdentifier) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        String searchTerm = keyword.toLowerCase().trim();
        List<Episode> allEpisodes = getCachedEpisodes();

        // Filter by show if identifier provided
        if (showIdentifier != null && !showIdentifier.trim().isEmpty()) {
            String resolvedShowId = resolveShowIdentifier(showIdentifier);
            allEpisodes = allEpisodes.stream()
                    .filter(episode -> resolvedShowId.equals(episode.showId()))
                    .toList();
        }

        return allEpisodes.stream()
                .filter(episode -> matchesKeyword(episode, searchTerm))
                .sorted((e1, e2) -> e2.publishedAt().compareTo(e1.publishedAt()))
                .limit(Math.min(maxResults, 50))
                .toList();
    }

    public Episode getEpisodeById(String episodeId) {
        try {
            JsonNode response = restClient.get()
                    .uri("/episodes/{id}", episodeId)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("data")) {
                return parseEpisodeFromJson(response.get("data"));
            }

            throw new RuntimeException("Episode not found: " + episodeId);
        } catch (Exception e) {
            logger.error("Error fetching episode by ID: {}", episodeId, e);
            throw new RuntimeException("Failed to fetch episode: " + episodeId, e);
        }
    }

    public PodcastStats getPodcastStats() {
        List<Show> allShows = getCachedShows();
        List<Episode> allEpisodes = getCachedEpisodes();

        if (allEpisodes.isEmpty()) {
            return new PodcastStats(allShows.size(), 0, null, null, 0, 0, 0.0, List.of());
        }

        // Sort episodes by date
        List<Episode> sortedEpisodes = allEpisodes.stream()
                .filter(Episode::isPublished)
                .sorted(Comparator.comparing(Episode::publishedAt))
                .toList();

        LocalDateTime firstEpisode = sortedEpisodes.get(0).publishedAt();
        LocalDateTime latestEpisode = sortedEpisodes.get(sortedEpisodes.size() - 1).publishedAt();
        String latestEpisodeTitle = sortedEpisodes.get(sortedEpisodes.size() - 1).title();
        LocalDateTime now = LocalDateTime.now();

        // Calculate episodes this year and month
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        int episodesThisYear = (int) sortedEpisodes.stream()
                .filter(episode -> episode.publishedAt().getYear() == currentYear)
                .count();

        int episodesThisMonth = (int) sortedEpisodes.stream()
                .filter(episode -> episode.publishedAt().getYear() == currentYear &&
                                 episode.publishedAt().getMonthValue() == currentMonth)
                .count();

        // Calculate average episodes per month
        long monthsBetween = ChronoUnit.MONTHS.between(firstEpisode, latestEpisode) + 1;
        double averageEpisodesPerMonth = monthsBetween > 0 ? (double) sortedEpisodes.size() / monthsBetween : 0;

        // Create show summaries
        List<PodcastStats.ShowSummary> showSummaries = allShows.stream()
                .map(show -> {
                    List<Episode> showEpisodes = sortedEpisodes.stream()
                            .filter(ep -> show.id().equals(ep.showId()))
                            .toList();

                    LocalDateTime latestShowEpisode = showEpisodes.isEmpty() ? null :
                            showEpisodes.stream()
                                    .map(Episode::publishedAt)
                                    .max(LocalDateTime::compareTo)
                                    .orElse(null);

                    return new PodcastStats.ShowSummary(
                            show.title(),
                            showEpisodes.size(),
                            latestShowEpisode
                    );
                })
                .filter(summary -> summary.episodeCount() > 0)
                .toList();

        return new PodcastStats(
                allShows.size(),
                sortedEpisodes.size(),
                latestEpisode,
                latestEpisodeTitle,
                episodesThisYear,
                episodesThisMonth,
                averageEpisodesPerMonth,
                showSummaries
        );
    }

    @SuppressWarnings("unchecked")
    private List<Show> getCachedShows() {
        if (needsCacheRefresh()) {
            refreshCache();
        }
        return (List<Show>) cache.getOrDefault("shows", List.of());
    }

    /**
     * Get cached episodes, refreshing cache if needed
     */
    @SuppressWarnings("unchecked")
    private List<Episode> getCachedEpisodes() {
        if (needsCacheRefresh()) {
            refreshCache();
        }
        return (List<Episode>) cache.getOrDefault("episodes", List.of());
    }

    private boolean needsCacheRefresh() {
        return lastCacheTime == null ||
               ChronoUnit.MINUTES.between(lastCacheTime, LocalDateTime.now()) >= podcastProperties.getCacheDurationMinutes() ||
               !cache.containsKey("shows") ||
               !cache.containsKey("episodes");
    }

    private void refreshCache() {
        try {
            List<Show> shows = fetchShowsFromApi();
            List<Episode> episodes = fetchEpisodesFromApi();

            cache.put("shows", shows);
            cache.put("episodes", episodes);
            lastCacheTime = LocalDateTime.now();

            logger.info("Podcast cache refreshed with {} shows and {} episodes", shows.size(), episodes.size());
        } catch (Exception e) {
            logger.error("Failed to refresh podcast cache", e);
            // Keep existing cache if refresh fails
        }
    }

    private List<Show> fetchShowsFromApi() {
        try {
            logger.info("Fetching shows from Transistor API");

            JsonNode response = restClient.get()
                    .uri("/shows")
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("data")) {
                JsonNode data = response.get("data");
                List<Show> shows = StreamSupport.stream(data.spliterator(), false)
                        .map(this::parseShowFromJson)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                logger.info("Successfully fetched {} shows", shows.size());
                return shows;
            }

            return List.of();
        } catch (Exception e) {
            logger.error("Error fetching shows from API", e);
            throw new RuntimeException("Failed to fetch shows from Transistor API", e);
        }
    }

    private List<Episode> fetchEpisodesFromApi() {
        try {
            logger.info("Fetching episodes from Transistor API");

            List<Episode> allEpisodes = new ArrayList<>();
            int page = 1;
            boolean hasMore = true;

            // Transistor API uses pagination
            while (hasMore && page <= 10) { // Limit to 10 pages to avoid infinite loops
                final int currentPage = page; // Create final variable for lambda
                JsonNode response = restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/episodes")
                                .queryParam("pagination[page]", currentPage)
                                .queryParam("pagination[per]", 50)
                                .build())
                        .retrieve()
                        .body(JsonNode.class);

                if (response != null && response.has("data")) {
                    JsonNode data = response.get("data");
                    List<Episode> pageEpisodes = StreamSupport.stream(data.spliterator(), false)
                            .map(this::parseEpisodeFromJson)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    allEpisodes.addAll(pageEpisodes);

                    // Check if there are more pages
                    JsonNode meta = response.get("meta");
                    if (meta != null && meta.has("currentPage") && meta.has("totalPages")) {
                        int responsePage = meta.get("currentPage").asInt();
                        int totalPages = meta.get("totalPages").asInt();
                        hasMore = responsePage < totalPages;
                        page++;
                    } else {
                        hasMore = false;
                    }
                } else {
                    hasMore = false;
                }
            }

            logger.info("Successfully fetched {} episodes", allEpisodes.size());
            return allEpisodes;
        } catch (Exception e) {
            logger.error("Error fetching episodes from API", e);
            throw new RuntimeException("Failed to fetch episodes from Transistor API", e);
        }
    }

    private Show parseShowFromJson(JsonNode data) {
        try {
            JsonNode attributes = data.get("attributes");
            if (attributes == null) return null;

            String id = data.get("id").asText();
            String title = attributes.has("title") ? attributes.get("title").asText() : null;
            String description = attributes.has("description") ? attributes.get("description").asText() : null;
            String author = attributes.has("author") ? attributes.get("author").asText() : null;
            String websiteUrl = attributes.has("website") ? attributes.get("website").asText() : null;
            String artworkUrl = attributes.has("artwork_url") ? attributes.get("artwork_url").asText() : null;
            String status = attributes.has("status") ? attributes.get("status").asText() : null;

            LocalDateTime createdAt = null;
            if (attributes.has("created_at")) {
                createdAt = parseDateTime(attributes.get("created_at").asText());
            }

            return new Show(id, title, description, author, websiteUrl, artworkUrl, status, createdAt);
        } catch (Exception e) {
            logger.warn("Failed to parse show from JSON: {}", e.getMessage());
            return null;
        }
    }

    private Episode parseEpisodeFromJson(JsonNode data) {
        try {
            JsonNode attributes = data.get("attributes");
            if (attributes == null) return null;

            String id = data.get("id").asText();
            String title = attributes.has("title") ? attributes.get("title").asText() : null;
            String description = attributes.has("summary") ? attributes.get("summary").asText() : null;
            String status = attributes.has("status") ? attributes.get("status").asText() : null;
            String audioUrl = attributes.has("media_url") ? attributes.get("media_url").asText() : null;
            String duration = attributes.has("duration") ? String.valueOf(attributes.get("duration").asInt()) + " seconds" : null;

            Integer season = attributes.has("season") && !attributes.get("season").isNull() ?
                    attributes.get("season").asInt() : null;
            Integer number = attributes.has("number") && !attributes.get("number").isNull() ?
                    attributes.get("number").asInt() : null;

            LocalDateTime publishedAt = null;
            if (attributes.has("published_at") && !attributes.get("published_at").isNull()) {
                publishedAt = parseDateTime(attributes.get("published_at").asText());
            }

            // Get show information from relationships
            String showId = null;
            String showTitle = null;
            if (data.has("relationships")) {
                JsonNode relationships = data.get("relationships");
                if (relationships.has("show") && relationships.get("show").has("data")) {
                    JsonNode showData = relationships.get("show").get("data");
                    showId = showData.has("id") ? showData.get("id").asText() : null;
                }
            }

            // Try to get show title from cache
            if (showId != null) {
                final String finalShowId = showId; // Create final variable for lambda
                List<Show> cachedShows = (List<Show>) cache.get("shows");
                if (cachedShows != null) {
                    Optional<Show> show = cachedShows.stream()
                            .filter(s -> s.id().equals(finalShowId))
                            .findFirst();
                    if (show.isPresent()) {
                        showTitle = show.get().title();
                    }
                }
            }

            return new Episode(id, title, description, showId, showTitle, publishedAt,
                             audioUrl, duration, status, season, number);
        } catch (Exception e) {
            logger.warn("Failed to parse episode from JSON: {}", e.getMessage());
            return null;
        }
    }

    private boolean matchesKeyword(Episode episode, String keyword) {
        String title = episode.title() != null ? episode.title().toLowerCase() : "";
        String description = episode.description() != null ? episode.description().toLowerCase() : "";
        return title.contains(keyword) || description.contains(keyword);
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        try {
            // Try parsing as ISO instant
            return LocalDateTime.ofInstant(
                    java.time.Instant.parse(dateTimeString),
                    ZoneId.systemDefault()
            );
        } catch (Exception e) {
            logger.warn("Failed to parse datetime: {}", dateTimeString);
            return LocalDateTime.now();
        }
    }
}
