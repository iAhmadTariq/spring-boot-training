package com.training.redmath.chat.controller;

import com.training.redmath.chat.service.ChatService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

  final private ChatService chatService;

  public ChatController(ChatService chatService, ChatMemory chatMemory) {
    this.chatService = chatService;
  }

  @GetMapping("/api/v1/chat")
  public String chat(@RequestParam(name = "message", defaultValue = "Hi") String message) {
    return chatService.chat(message);
  }

}
