package dev.danvega.dvaas.tools.blog.model;

import java.time.LocalDateTime;

public record BlogStats(
        int totalPosts,
        LocalDateTime firstPostDate,
        LocalDateTime latestPostDate,
        int postsThisYear,
        int postsThisMonth,
        double averagePostsPerMonth,
        int postsWithYouTubeVideos,
        String mostCommonTag
) {

    public String getBlogTimespan() {
        if (firstPostDate == null || latestPostDate == null) {
            return "Unknown timespan";
        }

        int yearsDiff = latestPostDate.getYear() - firstPostDate.getYear();
        if (yearsDiff == 0) {
            return "Started in " + firstPostDate.getYear();
        }
        return String.format("%d years (%d - %d)", yearsDiff, firstPostDate.getYear(), latestPostDate.getYear());
    }

    public String getPostingFrequency() {
        if (averagePostsPerMonth >= 4) {
            return "Very active (4+ posts/month)";
        } else if (averagePostsPerMonth >= 2) {
            return "Active (2-4 posts/month)";
        } else if (averagePostsPerMonth >= 1) {
            return "Regular (1-2 posts/month)";
        } else {
            return "Occasional (less than 1 post/month)";
        }
    }

    public String getYouTubeIntegrationPercentage() {
        if (totalPosts == 0) {
            return "0%";
        }
        double percentage = (postsWithYouTubeVideos * 100.0) / totalPosts;
        return String.format("%.1f%%", percentage);
    }
}