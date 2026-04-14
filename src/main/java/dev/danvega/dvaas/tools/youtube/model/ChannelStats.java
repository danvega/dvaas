package dev.danvega.dvaas.tools.youtube.model;

import java.time.LocalDateTime;

public record ChannelStats(
        String channelId,
        String title,
        String description,
        long subscriberCount,
        long totalViewCount,
        long videoCount,
        LocalDateTime channelCreatedAt,
        boolean subscriberCountHidden
) {

    public String getFormattedSubscriberCount() {
        if (subscriberCountHidden) {
            return "Hidden";
        }

        if (subscriberCount >= 1_000_000) {
            return String.format("%.1fM", subscriberCount / 1_000_000.0);
        } else if (subscriberCount >= 1_000) {
            return String.format("%.1fK", subscriberCount / 1_000.0);
        }
        return String.valueOf(subscriberCount);
    }

    public String getFormattedTotalViewCount() {
        if (totalViewCount >= 1_000_000_000) {
            return String.format("%.1fB", totalViewCount / 1_000_000_000.0);
        } else if (totalViewCount >= 1_000_000) {
            return String.format("%.1fM", totalViewCount / 1_000_000.0);
        } else if (totalViewCount >= 1_000) {
            return String.format("%.1fK", totalViewCount / 1_000.0);
        }
        return String.valueOf(totalViewCount);
    }

    public long getAverageViewsPerVideo() {
        return videoCount > 0 ? totalViewCount / videoCount : 0;
    }
}