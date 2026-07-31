package com.redmath.training.chat;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class RagChatController {

  private final RagChatService ragChatService;

  public RagChatController(RagChatService ragChatService) {
    this.ragChatService = ragChatService;
  }

  @PostMapping
  public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
//    String answer = ragChatService.answer(request.question(), request.conversationId());
    return ResponseEntity.ok(new ChatResponse("answer"));
  }
}
