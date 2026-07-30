package dev.danvega.dvaas.tools.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import dev.danvega.dvaas.config.YouTubeProperties;
import dev.danvega.dvaas.tools.youtube.model.ChannelStats;
import dev.danvega.dvaas.tools.youtube.model.Video;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = {"dvaas.youtube.api-key", "dvaas.youtube.channel-id"})
public class YouTubeService {

    private static final Logger logger = LoggerFactory.getLogger(YouTubeService.class);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final int MAX_UPLOADS_POOL = 1000;
    private static final int STATS_BATCH_SIZE = 50;

    private final YouTube youtube;
    private final YouTubeProperties youTubeProperties;

    public YouTubeService(YouTubeProperties youTubeProperties) throws GeneralSecurityException, IOException {
        this.youTubeProperties = youTubeProperties;

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        this.youtube = new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(youTubeProperties.applicationName())
                .build();

        logger.info("YouTube service initialized for channel: {}", youTubeProperties.channelId());
    }

    public ChannelStats getChannelStats() {
        try {
            YouTube.Channels.List request = youtube.channels()
                    .list(List.of("statistics", "snippet"))
                    .setId(List.of(youTubeProperties.channelId()))
                    .setKey(youTubeProperties.apiKey());

            ChannelListResponse response = request.execute();

            if (response.getItems() == null || response.getItems().isEmpty()) {
                throw new RuntimeException("Channel not found: " + youTubeProperties.channelId());
            }

            Channel channel = response.getItems().get(0);
            ChannelStatistics stats = channel.getStatistics();
            ChannelSnippet snippet = channel.getSnippet();

            return new ChannelStats(
                    channel.getId(),
                    snippet.getTitle(),
                    snippet.getDescription(),
                    stats.getSubscriberCount() != null ? stats.getSubscriberCount().longValue() : 0,
                    stats.getViewCount() != null ? stats.getViewCount().longValue() : 0,
                    stats.getVideoCount() != null ? stats.getVideoCount().longValue() : 0,
                    parseDateTime(snippet.getPublishedAt().toString()),
                    stats.getHiddenSubscriberCount() != null && stats.getHiddenSubscriberCount()
            );
        } catch (IOException e) {
            logger.error("Error fetching channel stats", e);
            throw new RuntimeException("Failed to fetch channel statistics", e);
        }
    }

    public List<Video> getLatestVideos(int maxResults) {
        try {
            List<Video> videos = fetchUploads(null, Math.min(maxResults, 50));
            return getVideoStatistics(videos);
        } catch (IOException e) {
            logger.error("Error fetching latest videos", e);
            throw new RuntimeException("Failed to fetch latest videos", e);
        }
    }

    public List<Video> getTopVideos(int maxResults, String timeRange) {
        try {
            List<Video> pool = switch (timeRange != null ? timeRange : "recent") {
                case "month" -> fetchUploads(LocalDateTime.now().minusDays(30), MAX_UPLOADS_POOL);
                case "year" -> fetchUploads(LocalDateTime.now().minusDays(365), MAX_UPLOADS_POOL);
                case "all" -> fetchUploads(null, MAX_UPLOADS_POOL);
                default -> fetchUploads(null, 50);
            };

            return getVideoStatistics(pool).stream()
                    .sorted((v1, v2) -> Long.compare(v2.viewCount(), v1.viewCount()))
                    .limit(maxResults)
                    .toList();
        } catch (Exception e) {
            logger.error("Error fetching top videos", e);
            throw new RuntimeException("Failed to fetch top videos", e);
        }
    }

    // The uploads playlist is ordered newest-first, so pagination can stop
    // as soon as an item falls before the cutoff date.
    private List<Video> fetchUploads(LocalDateTime publishedAfter, int maxVideos) throws IOException {
        String uploadsPlaylistId = getUploadsPlaylistId();
        List<Video> videos = new ArrayList<>();
        String pageToken = null;

        do {
            YouTube.PlaylistItems.List request = youtube.playlistItems()
                    .list(List.of("snippet", "contentDetails"))
                    .setPlaylistId(uploadsPlaylistId)
                    .setMaxResults(50L)
                    .setKey(youTubeProperties.apiKey());
            if (pageToken != null) {
                request.setPageToken(pageToken);
            }

            PlaylistItemListResponse response = request.execute();
            for (PlaylistItem item : response.getItems() != null ? response.getItems() : List.<PlaylistItem>of()) {
                Video video = convertPlaylistItemToVideoInfo(item);
                if (publishedAfter != null && video.publishedAt().isBefore(publishedAfter)) {
                    return videos;
                }
                videos.add(video);
                if (videos.size() >= maxVideos) {
                    return videos;
                }
            }
            pageToken = response.getNextPageToken();
        } while (pageToken != null);

        return videos;
    }

    public List<Video> searchVideosByTopic(String topic, int maxResults) {
        try {
            YouTube.Search.List search = youtube.search()
                    .list(List.of("snippet"))
                    .setQ(topic)
                    .setChannelId(youTubeProperties.channelId())
                    .setType(List.of("video"))
                    .setOrder("relevance")
                    .setMaxResults((long) Math.min(maxResults, 50))
                    .setKey(youTubeProperties.apiKey());

            SearchListResponse searchResponse = search.execute();

            return convertSearchResultsToVideoInfo(searchResponse.getItems());
        } catch (IOException e) {
            logger.error("Error searching videos for topic: {}", topic, e);
            throw new RuntimeException("Failed to search videos for topic: " + topic, e);
        }
    }

    private String getUploadsPlaylistId() throws IOException {
        YouTube.Channels.List request = youtube.channels()
                .list(List.of("contentDetails"))
                .setId(List.of(youTubeProperties.channelId()))
                .setKey(youTubeProperties.apiKey());

        ChannelListResponse response = request.execute();

        if (response.getItems() == null || response.getItems().isEmpty()) {
            throw new RuntimeException("Channel not found: " + youTubeProperties.channelId());
        }

        return response.getItems().get(0).getContentDetails().getRelatedPlaylists().getUploads();
    }

    private Video convertPlaylistItemToVideoInfo(PlaylistItem item) {
        PlaylistItemSnippet snippet = item.getSnippet();
        String videoId = snippet.getResourceId().getVideoId();

        return Video.basic(
                videoId,
                snippet.getTitle(),
                "https://www.youtube.com/watch?v=" + videoId,
                parseDateTime(snippet.getPublishedAt().toString()),
                0 // View count will be populated separately if needed
        );
    }

    private List<Video> convertSearchResultsToVideoInfo(List<com.google.api.services.youtube.model.SearchResult> items) {
        if (items == null) return new ArrayList<>();

        return items.stream()
                .map(this::convertSearchResultToVideoInfo)
                .toList();
    }

    private Video convertSearchResultToVideoInfo(com.google.api.services.youtube.model.SearchResult item) {
        SearchResultSnippet snippet = item.getSnippet();
        String videoId = item.getId().getVideoId();

        return Video.basic(
                videoId,
                snippet.getTitle(),
                "https://www.youtube.com/watch?v=" + videoId,
                parseDateTime(snippet.getPublishedAt().toString()),
                0 // View count will be populated separately if needed
        );
    }

    private List<Video> getVideoStatistics(List<Video> videos) throws IOException {
        if (videos.isEmpty()) return videos;

        List<String> videoIds = videos.stream().map(Video::id).toList();

        // The videos.list endpoint accepts at most 50 ids per request
        Map<String, com.google.api.services.youtube.model.Video> statsById = new HashMap<>();
        for (int i = 0; i < videoIds.size(); i += STATS_BATCH_SIZE) {
            List<String> batch = videoIds.subList(i, Math.min(i + STATS_BATCH_SIZE, videoIds.size()));

            YouTube.Videos.List request = youtube.videos()
                    .list(List.of("statistics", "contentDetails"))
                    .setId(batch)
                    .setKey(youTubeProperties.apiKey());

            VideoListResponse response = request.execute();
            if (response.getItems() != null) {
                response.getItems().forEach(v -> statsById.put(v.getId(), v));
            }
        }

        return videos.stream()
                .map(video -> {
                    com.google.api.services.youtube.model.Video youtubeVideo = statsById.get(video.id());

                    if (youtubeVideo != null && youtubeVideo.getStatistics() != null) {
                        VideoStatistics stats = youtubeVideo.getStatistics();
                        return new Video(
                                video.id(),
                                video.title(),
                                video.url(),
                                video.description(),
                                video.publishedAt(),
                                stats.getViewCount() != null ? stats.getViewCount().longValue() : 0,
                                stats.getLikeCount() != null ? stats.getLikeCount().longValue() : 0,
                                stats.getCommentCount() != null ? stats.getCommentCount().longValue() : 0,
                                youtubeVideo.getContentDetails() != null ?
                                    youtubeVideo.getContentDetails().getDuration() : null,
                                video.thumbnailUrl()
                        );
                    }
                    return video;
                })
                .toList();
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        try {
            // YouTube API returns ISO 8601 format
            return LocalDateTime.parse(dateTimeString.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            logger.warn("Failed to parse datetime: {}", dateTimeString);
            return LocalDateTime.now();
        }
    }
}