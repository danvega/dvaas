package dev.danvega.dvaas.tools.newsletter.model;

public record PostStats(
        long opens,
        long clicks,
        long uniqueOpens,
        long uniqueClicks
) {

    public double getOpenRate(long totalRecipients) {
        if (totalRecipients == 0) return 0.0;
        return (uniqueOpens * 100.0) / totalRecipients;
    }

    public double getClickRate(long totalRecipients) {
        if (totalRecipients == 0) return 0.0;
        return (uniqueClicks * 100.0) / totalRecipients;
    }

    public double getClickToOpenRate() {
        if (uniqueOpens == 0) return 0.0;
        return (uniqueClicks * 100.0) / uniqueOpens;
    }

    public static PostStats empty() {
        return new PostStats(0, 0, 0, 0);
    }

    public boolean hasStats() {
        return opens > 0 || clicks > 0 || uniqueOpens > 0 || uniqueClicks > 0;
    }
}
