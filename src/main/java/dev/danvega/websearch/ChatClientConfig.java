package dev.danvega.websearch;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

@Configuration
public class ChatClientConfig {

    @Bean
    ChatClient springAiClient(OpenAiChatProperties chatProperties) {
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .apiKey(chatProperties.getApiKey())
                        .baseUrl(chatProperties.getBaseUrl())
                        .model(chatProperties.getModel())
                        .build()
                )
                .build();
        return ChatClient.builder(openAiChatModel).build();
    }

    @Profile("open-sdk")
    @Bean
    OpenAIClient openAiSdkClient(OpenAiChatProperties chatProperties) {
        return  OpenAIOkHttpClient.builder()
                .apiKey(chatProperties.getApiKey())
                .baseUrl(chatProperties.getBaseUrl())
                .timeout(Duration.ofSeconds(90))
                .build();
    }
}
