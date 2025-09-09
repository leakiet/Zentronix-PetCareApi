package com.petcare.portal.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;

@Configuration
public class AIConfig {

	@Bean
	public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
		return ChatClient.create(openAiChatModel);
	}
}
