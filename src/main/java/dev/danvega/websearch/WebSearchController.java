package dev.danvega.websearch;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/spring-ai")
@RestController
public class WebSearchController {

    private final ChatClient chatClient;

    public WebSearchController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/nemotron")
    public String nemotron() {
        OpenAiChatOptions.Builder options = OpenAiChatOptions.builder()
                .extraBody(Map.of("web_search_options",
                        Map.of("search_context_size", "low"))
                );

        return chatClient.prompt()
                .user("Tell me an interesting fact about nemotron-3.5")
                .options(OpenAiChatOptions.builder())
                .call()
                .content();
    }

    @GetMapping("/news")
    public String currentNews() {
        OpenAiChatOptions.Builder options = OpenAiChatOptions.builder()
                .extraBody(Map.of("web_search_options",
                        Map.of("search_context_size", "low"))
                );

        var systemPrompt = new ClassPathResource("prompts/news-system.st");
        var userPrompt = new ClassPathResource("prompts/news-user.st");

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .options(options)
                .call()
                .content();
    }

    @GetMapping("/restaurants")
    public String findRestaurants(@RequestParam(defaultValue = "any") String cuisine,
                                  @RequestParam(defaultValue = "$") String priceRange,
                                  @RequestParam(defaultValue = "Cleveland") String city,
                                  @RequestParam(defaultValue = "Ohio") String region,
                                  @RequestParam(defaultValue = "US") String country,
                                  @RequestParam(defaultValue = "America/New_York") String timezone) {

        Map<String, Object> webSearchOptions = Map.of(
                "search_context_size", "medium",
                "user_location", Map.of(
                        "type", "approximate",
                        "approximate", Map.of(
                                "city", city,
                                "region", region,
                                "country", country,
                                "timezone", timezone)));

        OpenAiChatOptions.Builder options = OpenAiChatOptions.builder()
                .extraBody(Map.of("web_search_options", webSearchOptions));

        var systemPrompt = SystemMessage.builder()
                .text(new ClassPathResource("prompts/restaurants-system.st"))
                .build();
        var userPrompt = SystemMessage.builder()
                .text(new ClassPathResource("prompts/restaurants-user.st"))
                .build();
        Map<String, Object> params = Map.of(
                "cuisine", cuisine,
                "price", this.getPriceRangeDescription(priceRange)
        );
        return chatClient.prompt(Prompt.builder().messages(systemPrompt, userPrompt).build())
                .system(sp -> sp.params(params))
                .user(up -> up.params(params))
//                .user(new ClassPathResource("prompts/restaurants-user.st").getContentAsString(StandardCharsets.UTF_8).formatted(cuisine, this.getPriceRangeDescription(priceRange)))
                .options(options)
                .call()
                .content();
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
