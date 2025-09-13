# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is "dvaas" (Dan Vega as a Service) - a Spring Boot 3.5.5 application that implements a Model Context Protocol (MCP) server using Spring AI and Anthropic Claude integration. The project demonstrates how to build AI-powered services with MCP capabilities in a Spring Boot environment, featuring YouTube integration for channel and video management.

## Build and Development Commands

### Maven Commands
- **Build the project**: `./mvnw clean compile`
- **Run tests**: `./mvnw test`
- **Run the application**: `./mvnw spring-boot:run`
- **Package as JAR**: `./mvnw package`
- **Clean build artifacts**: `./mvnw clean`

### Application Commands
- **Run specific test class**: `./mvnw test -Dtest=ApplicationTests`
- **Run with profile**: `./mvnw spring-boot:run -Dspring.profiles.active=dev`
- **Test MCP server**: Ensure ANTHROPIC_API_KEY, YOUTUBE_API_KEY, and YOUTUBE_CHANNEL_ID environment variables are set

## Architecture

### Technology Stack
- **Java 24** (preview features enabled)
- **Spring Boot 3.5.5** with Spring Web starter
- **Spring AI 1.1.0-M1** with Anthropic integration
- **Spring AI MCP Server WebMVC** for Model Context Protocol capabilities
- **Google YouTube Data API v3** for YouTube integration
- **Maven** for build management

### Project Structure
```
src/
├── main/java/dev/danvega/dvaas/
│   ├── Application.java               # Main Spring Boot application class
│   └── tools/youtube/
│       ├── YouTubeTools.java          # MCP tools for YouTube operations
│       ├── YouTubeService.java        # YouTube Data API service layer
│       └── model/
│           ├── VideoInfo.java         # Video information model
│           ├── ChannelStats.java      # Channel statistics model
│           └── SearchResult.java      # Search results model
├── main/resources/
│   └── application.properties         # Application and MCP server configuration
└── test/java/dev/danvega/dvaas/
    ├── ApplicationTests.java          # Basic application tests
    ├── YouTubeServiceTest.java        # YouTube service unit tests
    ├── YouTubeToolsTest.java          # YouTube tools unit tests
    └── YouTubeServiceIntegrationTest.java # YouTube integration tests
```

### Configuration
- Main configuration is in `application.properties`
- Application name: "dvaas"
- Anthropic API key configuration via environment variable
- YouTube API key and channel ID configuration via environment variables
- MCP server configuration (enabled, named "dvaas-mcp-server", sync type, streamable protocol)

### Dependencies
- Spring Boot Web Starter (REST/Web services)
- Spring AI Anthropic integration (for AI model interactions)
- Spring AI MCP Server WebMVC (for Model Context Protocol server capabilities)
- Google YouTube Data API v3 (for YouTube integration)
- Google API client libraries (for HTTP transport and JSON processing)
- Standard Spring Boot testing dependencies

## Key Points for Development
- Functional MCP server with YouTube integration implementation
- Uses Spring AI Community MCP annotations (@McpTool, @McpToolParam)
- Implements four YouTube MCP tools:
  - `youtube-get-latest-videos`: Get recent videos from Dan Vega's channel
  - `youtube-get-top-videos`: Get top performing videos by view count
  - `youtube-search-videos-by-topic`: Search videos by keyword/topic
  - `youtube-get-channel-stats`: Get channel statistics and information
- Uses Java 24 with latest Spring Boot features
- Ready for extending with additional MCP tools following the YouTubeTools pattern

## Spring AI Documentation

When asked about Spring AI features, configuration, APIs, or implementation details, use the Context7 MCP server to fetch the most current documentation:

### Instructions for Claude Code:
1. **Use Context7 MCP** to access Spring AI 1.1.0-M1 documentation
2. **Library ID**: `/spring-projects/spring-ai/v1_1_0_m1`
3. **Focus areas** relevant to this project:
   - Anthropic Claude integration and chat models
   - Chat client configuration and usage
   - **MCP Server implementation** with Spring AI Community annotations
   - **MCP Tools development** (@McpTool, @McpToolParam patterns)
   - MCP server configuration and deployment
   - Embedding models and vector stores
   - Retrieval Augmented Generation (RAG) patterns
   - Spring Boot auto-configuration for MCP servers

### When to use Spring AI documentation:
- Implementing AI-powered endpoints or services
- Configuring Anthropic Claude chat models
- **Creating new MCP tools** following YouTubeTools pattern
- **MCP server configuration** and troubleshooting
- Setting up vector stores or embeddings
- Troubleshooting Spring AI integration issues
- Adding new AI capabilities (audio, image, etc.)
- Understanding Spring AI MCP server best practices and patterns