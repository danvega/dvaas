package dev.danvega.dvaas.tools.newsletter.model;

import java.util.List;

public record PostSearchResult(
        List<Post> posts,
        int totalResults,
        String searchCriteria,
        String publicationFilter
) {

    public static PostSearchResult forKeyword(List<Post> posts, String keyword, String publication) {
        return new PostSearchResult(
                posts,
                posts.size(),
                "keyword: " + keyword,
                publication
        );
    }

    public static PostSearchResult forStatus(List<Post> posts, String status, String publication) {
        return new PostSearchResult(
                posts,
                posts.size(),
                "status: " + status,
                publication
        );
    }

    public static PostSearchResult forLatest(List<Post> posts, String publication) {
        return new PostSearchResult(
                posts,
                posts.size(),
                "latest posts",
                publication
        );
    }

    public boolean hasResults() {
        return posts != null && !posts.isEmpty();
    }

    public int getResultCount() {
        return posts != null ? posts.size() : 0;
    }
}
