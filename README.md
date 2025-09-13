# DVaaS - Dan Vega as a Service

A Spring Boot 3.5.5 application that implements a Model Context Protocol (MCP) server using Spring AI and Anthropic Claude integration. This project demonstrates how to build AI-powered services with MCP capabilities in a Spring Boot environment.

## Features

- **MCP Server**: Implements Model Context Protocol server functionality
- **Spring AI Integration**: Uses Spring AI 1.1.0-M1 with Anthropic Claude
- **YouTube Integration**: Provides MCP tools for YouTube channel operations and video management
- **Blog Integration**: Provides MCP tools for RSS feed parsing and blog post management
- **Configuration Validation**: Jakarta Bean Validation for robust configuration management
- **Java 24**: Utilizes the latest Java features with preview support

## Prerequisites

- Java 24
- Maven 3.6+
- Anthropic API Key
- YouTube Data API Key (for YouTube features)
- YouTube Channel ID (for YouTube features)
- RSS Feed URL (for blog features)

## Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd dvaas
   ```

2. **Set up environment variables**
   ```bash
   export ANTHROPIC_API_KEY=your_anthropic_api_key_here

   # YouTube features (optional)
   export YOUTUBE_API_KEY=your_youtube_api_key_here
   export YOUTUBE_CHANNEL_ID=your_youtube_channel_id_here

   # Blog RSS URL is configured in application.properties
   ```

3. **Build the project**
   ```bash
   ./mvnw clean compile
   ```

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

## MCP Server Configuration

The application is configured as an MCP server with the following settings:

- **Server Name**: `dvaas-mcp-server`
- **Version**: `0.0.1`
- **Type**: `SYNC`
- **Protocol**: `streamable`

## Available MCP Tools

The application provides **8 MCP tools** organized by feature area:

### 🎥 YouTube Tools (4 tools)

Tools for YouTube channel operations and video management.

#### youtube-get-latest-videos
Get the most recent videos from Dan Vega's YouTube channel.

**Parameters:**
- `count` (optional): Number of videos to retrieve (default: 10, max: 50)

#### youtube-get-top-videos
Get the top-performing videos from Dan Vega's YouTube channel by view count.

**Parameters:**
- `count` (optional): Number of videos to retrieve (default: 10, max: 50)
- `timeRange` (optional): Time range filter - 'recent', 'month', 'year', 'all' (default: 'recent')

#### youtube-search-videos-by-topic
Search for videos on Dan Vega's YouTube channel by topic or keyword.

**Parameters:**
- `topic` (required): Topic or keyword to search for (e.g., 'java', 'spring', 'spring-ai')
- `count` (optional): Number of videos to retrieve (default: 10, max: 50)

#### youtube-get-channel-stats
Get overall statistics and information about Dan Vega's YouTube channel.

**Parameters:** None

### 📝 Blog Tools (4 tools)

Tools for RSS feed parsing and blog post management.

#### blog-get-latest-posts
Get the most recent blog posts from Dan Vega's RSS feed.

**Parameters:**
- `count` (required): Number of posts to retrieve (max: 50)

#### blog-search-posts-by-keyword
Search blog posts by keyword in title and description.

**Parameters:**
- `keyword` (required): Keyword to search for (e.g., 'spring', 'java', 'ai')
- `maxResults` (required): Maximum number of results to return (max: 50)

#### blog-get-posts-by-date-range
Get blog posts within a specific date range or year.

**Parameters:**
- `dateRange` (required): Date range in format 'YYYY' for year or 'YYYY-MM-DD to YYYY-MM-DD' for custom range
- `maxResults` (required): Maximum number of results to return (max: 50)

#### blog-get-stats
Get comprehensive statistics about the blog including total posts, posting frequency, and trends.

**Parameters:** None

### 🔮 Future Tools

The architecture is designed to easily support additional tool categories such as:
- **Social Media Tools**: Twitter/X integration, LinkedIn posts
- **Content Tools**: Newsletter management, course information
- **Analytics Tools**: Cross-platform analytics and insights
- **Community Tools**: Discord/Slack integration, community metrics

## Development

### Build Commands

```bash
# Clean and compile
./mvnw clean compile

# Run tests
./mvnw test

# Package as JAR
./mvnw package

# Run with specific profile
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

### Testing

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ApplicationTests
```

## Technology Stack

- **Java 24** (preview features enabled)
- **Spring Boot 3.5.5** with Spring Web starter
- **Spring AI 1.1.0-M1** with Anthropic integration
- **Spring AI MCP Server WebMVC** for MCP capabilities
- **Google YouTube Data API v3** for YouTube integration
- **Rome RSS Library v2.1.0** for RSS feed parsing
- **Jakarta Bean Validation** for configuration validation
- **Maven** for build management

## Project Structure

```
src/
├── main/java/dev/danvega/dvaas/
│   ├── Application.java              # Main Spring Boot application class
│   ├── config/
│   │   ├── DvaasConfiguration.java   # Main configuration class
│   │   ├── BlogProperties.java       # Blog configuration properties
│   │   └── YouTubeProperties.java    # YouTube configuration properties
│   └── tools/
│       ├── blog/
│       │   ├── BlogTools.java        # MCP tools for blog operations
│       │   ├── BlogService.java      # RSS feed service layer
│       │   └── model/
│       │       ├── BlogPost.java     # Blog post model
│       │       ├── BlogStats.java    # Blog statistics model
│       │       └── BlogSearchResult.java # Blog search results model
│       └── youtube/
│           ├── YouTubeTools.java     # MCP tools for YouTube operations
│           ├── YouTubeService.java   # YouTube Data API service layer
│           └── model/
│               ├── VideoInfo.java    # Video information model
│               ├── ChannelStats.java # Channel statistics model
│               └── SearchResult.java # Search results model
├── main/resources/
│   └── application.properties        # Application and MCP server configuration
└── test/java/dev/danvega/dvaas/
    ├── ApplicationTests.java         # Basic application tests
    └── tools/
        ├── blog/                     # Blog tools tests
        │   ├── BlogServiceTest.java
        │   ├── BlogToolsTest.java
        │   ├── BlogServiceIntegrationTest.java
        │   └── BlogIntegrationTest.java
        └── youtube/                  # YouTube tools tests
            ├── YouTubeServiceTest.java
            ├── YouTubeToolsTest.java
            └── YouTubeServiceIntegrationTest.java
```

## Configuration

The application uses **strongly-typed configuration properties** with validation for better maintainability and error prevention.

### Core Configuration

Key configuration properties in `application.properties`:

```properties
spring.application.name=dvaas
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}

# MCP Server Configuration
spring.ai.mcp.server.enabled=true
spring.ai.mcp.server.name=dvaas-mcp-server
spring.ai.mcp.server.version=0.0.1
spring.ai.mcp.server.type=SYNC
spring.ai.mcp.server.protocol=streamable

# YouTube Configuration (optional)
dvaas.youtube.api-key=${YOUTUBE_API_KEY}
dvaas.youtube.channel-id=${YOUTUBE_CHANNEL_ID}
dvaas.youtube.application-name=dvaas-youtube-mcp

# Blog Configuration
dvaas.blog.rss-url=https://www.danvega.dev/rss.xml
dvaas.blog.cache-duration=PT30M
```

### Configuration Validation

The application includes Jakarta Bean Validation:

- **BlogProperties**: Validates RSS URL format and cache duration (minimum 1 minute)
- **YouTubeProperties**: Validates API key length and YouTube channel ID format (24 characters starting with UC/UU/HC)
- **Startup validation**: Invalid configuration prevents application startup with clear error messages

### Feature-based Configuration

- **YouTube tools** are conditionally loaded when `dvaas.youtube.api-key` is configured
- **Blog tools** are conditionally loaded when `dvaas.blog.rss-url` is configured
- Tools can be enabled/disabled independently based on available configuration