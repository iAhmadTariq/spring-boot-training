package com.training.redmath.chat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

  private final ChatClient chatClient;

  public ChatService(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  public String chat(String userMessage) {
    return chatClient.prompt(userMessage)
        .advisors(context -> context.param(ChatMemory.CONVERSATION_ID, "default"))
        .call().content();
  }
}