# Spring AI + OpenAI Web Search

A Spring Boot application demonstrating real-time web search capabilities using Spring AI and OpenAI's web search functionality. This project showcases how to integrate OpenAI's GPT-4 search models to access up-to-date information from the web.

## Features

- **Real-time Web Search**: Query current information from the web using OpenAI's search-enabled models
- **Spring Framework Integration**: Built with Spring Boot and Spring AI for enterprise-ready architecture
- **Multiple Search Endpoints**: Different endpoints for various use cases:
  - `/gpt5` - Get current information about GPT-5
  - `/news` - Fetch the latest Spring Framework ecosystem news
  - `/restaurants` - Find local restaurant recommendations with location-based search

## Tech Stack

- **Spring Boot** 3.5.5
- **Spring AI** 1.0.1
- **OpenAI GPT-4o Search Preview** model
- **Java** 24
- **Maven** for dependency management

## Prerequisites

- Java 24 or higher
- Maven 3.6+
- OpenAI API key with web search access

## Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd websearch
   ```

2. **Set up environment variables**
   ```bash
   export OPENAI_API_KEY=your_openai_api_key_here
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

The application will start on `http://localhost:8080`

## API Endpoints

### GET `/gpt5`
Returns interesting facts about GPT-5 from current web sources.

**Example:**
```bash
curl http://localhost:8080/gpt5
```

### GET `/news`
Fetches the latest Spring Framework ecosystem news from the past 7 days, including updates on Spring Boot, Spring Security, Spring Cloud, and more.

**Example:**
```bash
curl http://localhost:8080/news
```

### GET `/restaurants`
Finds restaurant recommendations based on location and preferences.

**Parameters:**
- `cuisine` (optional, default: "any") - Type of cuisine
- `priceRange` (optional, default: "$") - Price range ($, $$, $$$, $$$$)
- `city` (optional, default: "Cleveland") - City name
- `region` (optional, default: "Ohio") - State/region
- `country` (optional, default: "US") - Country
- `timezone` (optional, default: "America/New_York") - Timezone

**Example:**
```bash
curl "http://localhost:8080/restaurants?cuisine=Italian&priceRange=$$&city=New York&region=New York"
```

## Configuration

The application uses OpenAI's `gpt-4o-search-preview` model with configurable web search options:

- **Search Context Size**: LOW, MEDIUM, or HIGH
- **Location Context**: Supports approximate location for location-aware searches
- **System Prompts**: Customized prompts for specific use cases

## Key Implementation Details

- **Environment-based API Key**: Reads OpenAI API key from environment variables
- **Custom Chat Options**: Configured with web search capabilities
- **Structured Prompts**: Uses system and user prompts for consistent, high-quality responses
- **Location Context**: Restaurant endpoint includes location data for accurate local results

## References

- [OpenAI Web Search Documentation](https://platform.openai.com/docs/guides/tools-web-search?api-mode=responses)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/docs/current/reference/html/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
