package dev.danvega.websearch;

import com.openai.client.OpenAIClient;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	 @Bean
     ApplicationRunner applicationRunner(OpenAiChatProperties chatProperties) {
	    return _ -> {
			String baseUrl = chatProperties.getOptions().getBaseUrl();
			String apiKey = chatProperties.getOptions().getApiKey();
			String model = chatProperties.getModel();
		};
	  }

}
