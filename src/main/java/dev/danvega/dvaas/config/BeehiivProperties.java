package dev.danvega.dvaas.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Map;

/**
 * Configuration properties for Beehiiv newsletter API integration
 * Supports multiple publications (e.g., danvega, bytesizedai)
 */
@ConfigurationProperties(prefix = "dvaas.beehiiv")
@Validated
public record BeehiivProperties(

        /**
         * Beehiiv API key for authentication
         * Must be a valid, non-empty API key
         */
        @NotBlank(message = "Beehiiv API key must not be blank")
        String apiKey,

        /**
         * Base URL for Beehiiv API
         * Must be a valid, non-empty URL starting with http:// or https://
         */
        @NotBlank(message = "Beehiiv base URL must not be blank")
        @Pattern(regexp = "^https?://.*", message = "Beehiiv base URL must start with http:// or https://")
        String baseUrl,

        /**
         * Cache duration for Beehiiv data
         * Must be at least 1 minute, default: 30 minutes
         */
        @NotNull(message = "Beehiiv cache duration must not be null")
        Duration cacheDuration,

        /**
         * Map of publication names to publication IDs
         * Example: {"danvega": "pub_xxx", "bytesizedai": "pub_yyy"}
         */
        @NotNull(message = "Beehiiv publications map must not be null")
        Map<String, String> publications

) {

    /**
     * Create default BeehiivProperties with sensible defaults and validation
     */
    public BeehiivProperties {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "https://api.beehiiv.com/v2";
        }

        if (cacheDuration == null) {
            cacheDuration = Duration.ofMinutes(30);
        }

        // Custom validation: cache duration must be at least 1 minute
        if (cacheDuration.toMinutes() < 1) {
            throw new IllegalArgumentException("Beehiiv cache duration must be at least 1 minute, got: " + cacheDuration);
        }

        // Validate publications map is not empty
        if (publications == null || publications.isEmpty()) {
            throw new IllegalArgumentException("Beehiiv publications map must contain at least one publication");
        }

        // Validate all publication IDs are not blank
        for (Map.Entry<String, String> entry : publications.entrySet()) {
            if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                throw new IllegalArgumentException("Beehiiv publication ID for '" + entry.getKey() + "' must not be blank");
            }
        }
    }

    /**
     * Get cache duration in minutes
     */
    public long getCacheDurationMinutes() {
        return cacheDuration.toMinutes();
    }

    /**
     * Check if Beehiiv integration is properly configured
     */
    public boolean isEnabled() {
        return apiKey != null && !apiKey.trim().isEmpty() &&
               baseUrl != null && !baseUrl.trim().isEmpty() &&
               publications != null && !publications.isEmpty();
    }

    /**
     * Get publication ID by name
     */
    public String getPublicationId(String publicationName) {
        return publications.get(publicationName);
    }

    /**
     * Check if a publication name exists
     */
    public boolean hasPublication(String publicationName) {
        return publications.containsKey(publicationName);
    }

    /**
     * Get all publication names
     */
    public java.util.Set<String> getPublicationNames() {
        return publications.keySet();
    }
}
