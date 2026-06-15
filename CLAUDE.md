# CLAUDE.md

dvaas (Dan Vega as a Service) — a Spring Boot MCP server built with Spring AI, exposing tools for YouTube, Blog (RSS), Speaking, Newsletter (Beehiiv), and Podcast (Transistor.fm).

## Stack

- Java 26, Spring Boot 4.1, Spring AI 2.0 (versions live in `pom.xml`)
- MCP tools use `@McpTool` / `@McpToolParam` annotations

## Commands

- Build: `./mvnw clean compile`
- Test: `./mvnw test`
- Run: `./mvnw spring-boot:run` (requires `ANTHROPIC_API_KEY`, `YOUTUBE_API_KEY`, `YOUTUBE_CHANNEL_ID` env vars)

## Structure

Each feature lives under `src/main/java/dev/danvega/dvaas/tools/<feature>/` with a `*Tools` class (MCP tools), a `*Service` class (API/data layer), and a `model/` package. Strongly-typed `@ConfigurationProperties` classes with Jakarta validation live in `config/`. Tools load conditionally based on available configuration. Follow these patterns when adding new tools.
