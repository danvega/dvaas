package dev.danvega.dvaas.tools.blog.model;

import java.time.LocalDateTime;
import java.util.List;

public record BlogPost(
        String title,
        String link,
        String guid,
        String description,
        LocalDateTime publishedAt,
        String author,
        List<String> tags,
        String youtubeVideoUrl
) {

    public static BlogPost basic(String title, String link, String guid, LocalDateTime publishedAt) {
        return new BlogPost(title, link, guid, null, publishedAt, null, List.of(), null);
    }

    public String getFullUrl() {
        if (link != null && link.startsWith("http")) {
            return link;
        }
        return "https://www.danvega.dev" + (link != null ? link : "");
    }

    public String getShortDescription() {
        if (description == null || description.isEmpty()) {
            return "";
        }
        return description.length() > 200 ? description.substring(0, 200) + "..." : description;
    }

    public boolean hasYouTubeVideo() {
        return youtubeVideoUrl != null && !youtubeVideoUrl.isEmpty();
    }

    public List<String> extractPotentialTags() {
        if (tags != null && !tags.isEmpty()) {
            return tags;
        }

        // Extract common tech terms from title and description as potential tags
        String content = (title + " " + (description != null ? description : "")).toLowerCase();
        return List.of("spring", "java", "boot", "ai", "graphql", "react", "vue", "docker", "kubernetes")
                .stream()
                .filter(tag -> content.contains(tag))
                .toList();
    }
}