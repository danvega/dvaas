package dev.danvega.dvaas.tools.podcast.model;

import java.time.LocalDateTime;
import java.util.List;

public record PodcastStats(
        int totalShows,
        int totalEpisodes,
        LocalDateTime latestEpisodeDate,
        String latestEpisodeTitle,
        int episodesThisYear,
        int episodesThisMonth,
        double averageEpisodesPerMonth,
        List<ShowSummary> showSummaries
) {

    public record ShowSummary(
            String showTitle,
            int episodeCount,
            LocalDateTime latestEpisode
    ) {}

    public boolean hasEpisodes() {
        return totalEpisodes > 0;
    }

    public String getFormattedAverageEpisodesPerMonth() {
        return String.format("%.1f", averageEpisodesPerMonth);
    }

    public String getMostActiveShow() {
        if (showSummaries == null || showSummaries.isEmpty()) {
            return "N/A";
        }

        return showSummaries.stream()
                .max((s1, s2) -> Integer.compare(s1.episodeCount(), s2.episodeCount()))
                .map(ShowSummary::showTitle)
                .orElse("N/A");
    }
}
