package dev.danvega.websearch;

import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.WebSearchTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Profile("open-sdk")
@RequestMapping("/openai-sdk")
@RestController
public class WebSearchOpenSdkController {
    @Value("${spring.ai.openai.chat.model}")
    private String model;

    private final OpenAIClient client;

    public WebSearchOpenSdkController(OpenAIClient client) {
        this.client = client;
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
        var systemPrompt = new ClassPathResource("prompts/news-system.st");
        var userPrompt = new ClassPathResource("prompts/news-user.st");
        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(model)
                .instructions(this.getPromptExternalized(systemPrompt))
                .input(this.getPromptExternalized(userPrompt))
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
        var systemPrompt = new ClassPathResource("prompts/restaurants-system.st");
        var userPrompt = new ClassPathResource("prompts/restaurants-user.st");
        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(model)
                .instructions(this.getPromptExternalized(systemPrompt, cuisine, this.getPriceRangeDescription(priceRange)))
                .input(this.getPromptExternalized(userPrompt, cuisine, this.getPriceRangeDescription(priceRange)))
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

    private String getPromptExternalized(Resource prompt, String... params) {
        try {
            return prompt.getContentAsString(StandardCharsets.UTF_8).formatted((Object[]) params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
