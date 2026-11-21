package dev.danvega.websearch;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.WebSearchTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RequestMapping("/openai-sdk")
@RestController
public class WebSearchOpenSdkController {
    @Value("${spring.ai.openai.chat.model}")
    private String model;

    private final OpenAIClient client;

    public WebSearchOpenSdkController() {
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(System.getenv("NVIDIA_API_KEY"))
                .baseUrl("https://integrate.api.nvidia.com/v1")
                .timeout(Duration.ofSeconds(90))
                .build();
    }

    @GetMapping("/nemotron")
    public String nemotron() {
        WebSearchTool webSearch = WebSearchTool.builder()
                .type(WebSearchTool.Type.WEB_SEARCH)
                .searchContextSize(WebSearchTool.SearchContextSize.LOW)
                .build();
        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(model)
                .input("Tell me an interesting fact about " + model)
                .addTool(webSearch)
                .build();
        return responseAsText(params);
    }

    @GetMapping("/news")
    public String currentNews() {
        WebSearchTool webSearch = WebSearchTool.builder()
                .type(WebSearchTool.Type.WEB_SEARCH)
                .searchContextSize(WebSearchTool.SearchContextSize.LOW)
                .build();

        String systemPrompt = """
                You are a Spring Framework expert and technical news analyst. Your role is to:
                
                1. Search for and identify the most recent and significant developments in the Spring ecosystem
                2. Focus on official announcements, major releases, security updates, and breaking changes
                3. Prioritize information from authoritative sources like:
                   - Official Spring blog (spring.io/blog)
                   - GitHub releases and changelogs
                   - Spring team announcements
                   - Major tech publications covering Spring
                
                4. Structure your response with:
                   - **Date and Source** for each news item
                   - **Impact Level** (Critical/Major/Minor)
                   - **Brief Summary** in 2-3 sentences
                   - **Action Items** for developers if applicable
                
                5. Filter out generic Java news unless directly Spring-related
                6. If no significant Spring news is found in the last week, expand to the last month
                7. Always verify information currency - ignore outdated articles
                
                Present the information in a clear, scannable format that busy developers can quickly digest.
                """;

        String userPrompt = """
                Search for the latest Spring Framework ecosystem news from the past 7 days, including:
                
                - Spring Boot releases and updates
                - Spring Security announcements  
                - Spring Cloud developments
                - New Spring projects or major updates
                - Breaking changes or deprecations
                - Security vulnerabilities and patches
                - Spring Tools and IDE integration updates
                - Performance improvements or new features
                - Community events or important blog posts
                
                Focus on news that would impact Spring developers in their daily work.
                If limited recent news, include significant developments from the past month.
                """;
        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(model)
                .instructions(systemPrompt)
                .input(userPrompt)
                .addTool(webSearch)
                .build();
        return responseAsText(params);
    }

    @GetMapping("/restaurants")
    public String findRestaurants(@RequestParam(defaultValue = "any") String cuisine,
                                  @RequestParam(defaultValue = "$") String priceRange,
                                  @RequestParam(defaultValue = "Cleveland") String city,
                                  @RequestParam(defaultValue = "Ohio") String region,
                                  @RequestParam(defaultValue = "US") String country,
                                  @RequestParam(defaultValue = "America/New_York") String timezone) {
        String systemPrompt = String.format("""
                        You are a local restaurant expert. You MUST provide exactly 3 restaurant recommendations - no more, no less.
                        
                        **CRITICAL REQUIREMENTS:**
                        - Return exactly 3 restaurants (never 1, 2, or more than 3)
                        - All restaurants must be currently OPEN and accepting customers
                        - All must match: %s cuisine and %s price range
                        - All must have recent positive reviews
                        
                        **Response Format for each of the 3 restaurants:**
                        **Restaurant Name** - Neighborhood
                        **Specialty** - What they're famous for
                        **Price & Rating** - Price range and rating
                        **Status & Contact** - Current hours and reservation info
                        
                        If you cannot find 3 restaurants that meet the exact criteria, expand your search to include:
                        1. Nearby neighborhoods
                        2. Slightly broader cuisine categories
                        3. Adjacent price ranges
                        
                        But you MUST return exactly 3 recommendations.
                        """,
                cuisine,
                getPriceRangeDescription(priceRange));

        String userPrompt = String.format("""
                        I need exactly 3 %s restaurant recommendations near me in the %s price range.
                        
                        **Requirements:**
                        - All 3 must be currently OPEN (not closed)
                        - All 3 must be accepting customers today
                        - All 3 should have good recent reviews
                        - Give me practical info: address, hours, phone, reservation needs
                        
                        **Important:** I need exactly 3 options - please don't give me fewer than 3 restaurants.
                        """,
                cuisine.equals("any") ? "great" : cuisine,
                getPriceRangeDescription(priceRange));

        // 1. Typed web search tool with user location
        WebSearchTool webSearch = WebSearchTool.builder()
                .type(WebSearchTool.Type.WEB_SEARCH)
                .searchContextSize(WebSearchTool.SearchContextSize.MEDIUM)
                .userLocation(WebSearchTool.UserLocation.builder()
                        .city(city)
                        .region(region)
                        .country(country)
                        .timezone(timezone)
                        .build())
                .build();
        // 2. Request: model + system prompt + user prompt + tool
        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(model)
                .instructions(systemPrompt)
                .input(userPrompt)
                .addTool(webSearch)
                .build();

        // 3. Call
        return responseAsText(params);
    }

    private String responseAsText(ResponseCreateParams params) {
        Response response = client.responses().create(params);
        return response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(ResponseOutputText::text)
                .reduce("", (a, b) -> a + b);
    }

    private String getPriceRangeDescription(String priceRange) {
        return switch (priceRange) {
            case "$" -> "budget-friendly";
            case "$$" -> "moderate";
            case "$$$" -> "upscale";
            case "$$$$" -> "fine dining";
            default -> "moderate";
        };
    }

}
