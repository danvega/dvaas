# DVaaS - Dan Vega as a Service

A Spring Boot 3.5.5 application that implements a Model Context Protocol (MCP) server using Spring AI and Anthropic Claude integration. This project demonstrates how to build AI-powered services with MCP capabilities in a Spring Boot environment.

## Features

- **MCP Server**: Implements Model Context Protocol server functionality
- **Spring AI Integration**: Uses Spring AI 1.1.0-M1 with Anthropic Claude
- **YouTube Integration**: Provides MCP tools for YouTube channel operations and video management
- **Java 24**: Utilizes the latest Java features with preview support

## Prerequisites

- Java 24
- Maven 3.6+
- Anthropic API Key
- YouTube Data API Key
- YouTube Channel ID (Dan Vega's channel ID)

## Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd dvaas
   ```

2. **Set up environment variables**
   ```bash
   export ANTHROPIC_API_KEY=your_anthropic_api_key_here
   export YOUTUBE_API_KEY=your_youtube_api_key_here
   export YOUTUBE_CHANNEL_ID=your_youtube_channel_id_here
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

### youtube-get-latest-videos

Get the most recent videos from Dan Vega's YouTube channel.

**Parameters:**
- `count` (optional): Number of videos to retrieve (default: 10, max: 50)

### youtube-get-top-videos

Get the top-performing videos from Dan Vega's YouTube channel by view count.

**Parameters:**
- `count` (optional): Number of videos to retrieve (default: 10, max: 50)
- `timeRange` (optional): Time range filter - 'recent', 'month', 'year', 'all' (default: 'recent')

### youtube-search-videos-by-topic

Search for videos on Dan Vega's YouTube channel by topic or keyword.

**Parameters:**
- `topic` (required): Topic or keyword to search for (e.g., 'java', 'spring', 'spring-ai')
- `count` (optional): Number of videos to retrieve (default: 10, max: 50)

### youtube-get-channel-stats

Get overall statistics and information about Dan Vega's YouTube channel.

**Parameters:** None

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
- **Spring Boot 3.5.5**
- **Spring AI 1.1.0-M1** with Anthropic integration
- **Spring AI MCP Server WebMVC** for MCP capabilities
- **Google YouTube Data API v3** for YouTube integration
- **Maven** for build management

## Project Structure

```
src/
├── main/java/dev/danvega/dvaas/
│   ├── Application.java              # Main Spring Boot application class
│   └── tools/youtube/
│       ├── YouTubeTools.java         # MCP tools for YouTube operations
│       ├── YouTubeService.java       # YouTube Data API service layer
│       └── model/
│           ├── VideoInfo.java        # Video information model
│           ├── ChannelStats.java     # Channel statistics model
│           └── SearchResult.java     # Search results model
├── main/resources/
│   └── application.properties        # Application and MCP server configuration
└── test/java/dev/danvega/dvaas/
    ├── ApplicationTests.java         # Basic application tests
    ├── YouTubeServiceTest.java       # YouTube service unit tests
    ├── YouTubeToolsTest.java         # YouTube tools unit tests
    └── YouTubeServiceIntegrationTest.java # YouTube integration tests
```

## Configuration

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

# YouTube Configuration
dvaas.youtube.api-key=${YOUTUBE_API_KEY}
dvaas.youtube.channel-id=${YOUTUBE_CHANNEL_ID}
```