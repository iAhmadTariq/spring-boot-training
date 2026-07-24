package com.training.redmath.chat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatConfiguration {

  @Bean
  ChatClient chatClient(ChatClient.Builder builder) {
    ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
    Advisor chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
    return builder.defaultAdvisors(chatMemoryAdvisor, new SimpleLoggerAdvisor()).build();
  }
}
